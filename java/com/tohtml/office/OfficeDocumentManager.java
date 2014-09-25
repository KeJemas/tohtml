package com.tohtml.office;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.ExternalOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.tohtml.util.FileUtil;




/**
 * 将Office文档转换为所需的文件类型,
 * 可以是Office2003-2007全部格式的文档. 包括.doc, .docx, .xls, .xlsx, .ppt, .pptx等.
 * 
 * 目标文件类型必须是该文档 用OpenOffice打开后“另存为”处可选的<保存类型>. 
 * 运行需要用到OpenOffice 3版本, 安装至默认路径.
 * 
 * @author pengsheng
 *
 */
public class OfficeDocumentManager {

	private static Log logger = LogFactory.getLog(OfficeManager.class);
	
	private static OfficeDocumentManager manager = null;
	private static OfficeManager openofficeManager = null;
	
	private static int defaultprot = 8100;// 转换文档默认端口
	private static boolean started = false;// Openoffice服务是否启动
	
	private static int conveterCount = 0;// 转换文档的个数
	private KillOpenofficeService killOpenofficeService = null;// 停止Openoffice服务释放内存线程
	
	private static String _content_replace_charset = "gb2312";
	
	public OfficeDocumentManager () {
		
		if (!started) {
			
			killOpenofficeService = new KillOpenofficeService();
			killOpenofficeService.setDaemon(true);
			killOpenofficeService.setPriority(Thread.MIN_PRIORITY);
			killOpenofficeService.start();
			
			startService();
			
			setContentReplaceCharset();
		}
	}
	
	public static synchronized OfficeDocumentManager getInstance () {
		
		if (manager == null)
			manager = new OfficeDocumentManager();
		
		return manager;
	}
	
	/**
	 *  转换office文档为Html文件
	 * @param inputFilePATH : 文档路径
	 * @return ： 转换后Html文件路径
	 * @throws IOException
	 */
	public String conveterOfficeDocument2Html(String inputFilePATH) throws IOException {
		return conveterOfficeDocument(inputFilePATH, "", "html");
	}
	
	/**
	 *  转换office文档为Html文件
	 * @param inputFilePATH : 文档路径
	 * @param outDir : 输出文件夹
	 * @return ： 转换后Html文件路径
	 * @throws IOException
	 */
	public String conveterOfficeDocument2Html(String inputFilePATH, String outDir) throws IOException {
		return conveterOfficeDocument(inputFilePATH, outDir, "html");
	}
	
	/**
	 *  转换office文档为其他类型文件
	 * @param inputFilePATH : 文档路径
	 * @param outDir : 输出文件夹
	 * @param destType : 转换后文件类型
	 * @return ： 转换后文档路径
	 * @throws IOException
	 */
	public String conveterOfficeDocument(String inputFilePATH, String outDir, String destType) throws IOException {

		File inputofficeFile = new File(inputFilePATH);
		if (inputofficeFile.exists()) {
			
			String fileType =  FilenameUtils.getExtension(inputofficeFile.getName());
			
			String uuid = "";
			String outputPATH = "";
			if ("".equals(outDir)) {
				
				uuid = UUID.randomUUID().toString().replace("-", "");
				outputPATH = inputofficeFile.getParentFile().getAbsolutePath() + File.separator + uuid + File.separator;
			} else {
				uuid = new File(outDir).getName();
				outputPATH = outDir + File.separator + uuid + File.separator;
			}
			
			if (openofficeManager != null) {
				
				OfficeDocumentConverter converter = new OfficeDocumentConverter(openofficeManager);

				conveterCount++;

				File outputofficeFile = new File(outputPATH + uuid + "." + fileType);

				FileUtil.copyFile(inputofficeFile, outputofficeFile); // 复制源文件至其他路径

				String outputFilePATH = outputPATH + uuid + "." + destType;
				File inputconveterFile = outputofficeFile;
				if (inputconveterFile.exists()) {

					File outputconveterFile = new File(outputFilePATH);
					if (!outputconveterFile.getParentFile().exists()) {
						outputconveterFile.getParentFile().mkdirs();
					}

					converter.convert(inputconveterFile, outputconveterFile); // 转换
					inputconveterFile.delete();

					// Word转换后内容居中
					if ("doc".equalsIgnoreCase(fileType)
							|| "docx".equalsIgnoreCase(fileType)) {
						
						FileUtil.replaceTextContent(
								outputFilePATH,
								"<BODY LANG=\"zh-CN\" TEXT=\"#000000\" DIR=\"LTR\">",
								"<BODY LANG=\"zh-CN\" TEXT=\"#000000\" DIR=\"LTR\" style=\"text-align: center;margin:auto;width:1000px;\">",
								_content_replace_charset);
					}

					return outputPATH;
				}
			}
		}
		
		return null;
	}
	
	/**
	 *  启动OpenOffice服务
	 */
	private void startService() {
		
		logger.info("准备启动Openoffice服务... ...");
		
		try {
			ExternalOfficeManagerConfiguration externalProcessOfficeManager = new ExternalOfficeManagerConfiguration();  
	        externalProcessOfficeManager.setConnectOnStart(true);  
	        externalProcessOfficeManager.setPortNumber(defaultprot);  
	        openofficeManager = externalProcessOfficeManager.buildOfficeManager();  
	        openofficeManager.start();  
			return;
		} catch (Exception e) {
			logger.info("尚未有启动的Openoffice服务... ...");
		}
		
		try {
			
			DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
			String officeHome = getOfficeHome();
			configuration.setOfficeHome(officeHome);
			
			configuration.setPortNumbers(defaultprot); // 设置转换端口，默认为8100  
	        configuration.setTaskExecutionTimeout(1000 * 60 * 3L);// 设置任务执行超时为3分钟  
	        configuration.setTaskQueueTimeout(1000 * 60 * 60 * 10L);// 设置任务队列超时为10小时  
	        configuration.setMaxTasksPerProcess(200);
	        
			openofficeManager = configuration.buildOfficeManager();
			openofficeManager.start();
			
			started = true;
			
			logger.info("启动Openoffice服务成功.");
			
		}catch (Exception e) {
			logger.info("启动Openoffice服务错误 ：" + e.getMessage());
		}
	}
	
	private void stopService() {

		logger.info("准备停止Openoffice服务... ...");
		try {
			
			if (openofficeManager != null) {
				openofficeManager.stop();
				started = false;
				
				killOpenofficeService.setbTerminate(true);
				killOpenofficeService.notify();
			}
			
			logger.info("停止Openoffice服务成功.");
			
		} catch (Exception e) {
			logger.info("停止Openoffice服务错误 ：" + e.getMessage());
		}
	}
	
	/**
	 *  当转换文档的个数超过500个 或者 当前时间为2-4点之间时，停止Openoffice服务用以释放内存
	 * @author pengsheng
	 *
	 */
	class KillOpenofficeService extends Thread {
		
		private boolean bTerminate = false;
		
		public KillOpenofficeService() {
			super("KillOpenofficeService");
		}
		
		public void run() {
			
			while (!bTerminate) {
				
				if (started) {
					
					int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					if ((curHour > 2 && curHour < 4) || conveterCount > 500) {
						stopService();
						conveterCount = 0;
					}
				}
				
				try {
					this.sleep(1200000);
				} catch (InterruptedException e) {
				}
			}
		}

		public boolean isbTerminate() {
			return bTerminate;
		}

		public void setbTerminate(boolean bTerminate) {
			this.bTerminate = bTerminate;
		}
	}
	
	/**
	 *  设置替换文本内容的字符编码
	 */
	private void setContentReplaceCharset() {
		String osName = System.getProperty("os.name");
		if (Pattern.matches("Windows.*", osName)) {
			_content_replace_charset = "gb2312";
		}else {
			_content_replace_charset = "utf8";
		}
	}
	
	/**
	 *  获取openoffice默认安装路径
	 * @return
	 */
	private String getOfficeHome() {
		
		String osName = System.getProperty("os.name");
		if (Pattern.matches("Linux.*", osName)) {
			return "/opt/openoffice.org3";
		} else if (Pattern.matches("Windows.*", osName)) {
			String osarch = System.getProperty("os.arch");
			if (osarch.indexOf("64") > 0) {
				return "C:/Program Files (x86)/OpenOffice.org 3";
			} else {
				return "C:/Program Files/OpenOffice.org 3";
			}
		} else if (Pattern.matches("Mac.*", osName)) {
			return "/Applications/OpenOffice.org.app/Contents";
		}
		
		return null;
	}
}

