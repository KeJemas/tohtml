package com.tohtml.txt;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Txt文件转换为Html
 * @author pengsheng
 *
 */
public class TxtManager {

	private static Log logger = LogFactory.getLog(TxtManager.class);
	
	private static String _content_charset = "gb2312";
	
	public TxtManager() {}
	
	/**
	 * 返回Html文件
	 * @param txtFileDir : txt文件路径
	 * @return
	 */
	public String conveterTxt2Html(String txtFileDir) {
		return conveterTxt2Html(txtFileDir, "");
	}
	
	/**
	 * 返回Html文件
	 * @param txtFileDir : txt文件路径
	 * @param outDir : 输出文件夹
	 * @return
	 */
	public String conveterTxt2Html(String txtFileDir, String outDir) {
		
		String htmlDir = "";
		String uuid = "";
		File txtFile = new File(txtFileDir);
		if (txtFile.exists()) {
			
			if ("".equals(outDir)) {
				uuid = UUID.randomUUID().toString().replace("-", "");
				htmlDir = txtFile.getParentFile().getAbsolutePath() + File.separator + uuid + File.separator;
			}else {
				uuid = new File(outDir).getName();
				htmlDir = outDir + File.separator + uuid + File.separator;
			}
		
			new File(htmlDir).mkdir();

			createHtml(txtFile, htmlDir + uuid + ".html");
		} 

		return htmlDir;
	}
	
	private void createHtml(File txtFile, String htmlFileDir) {
		
		if (txtFile.exists()) {
			
			StringBuffer html = new StringBuffer();
			html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">").append("\r\n");
			html.append("<html>").append("\r\n");
			html.append("<head>").append("\r\n");
			html.append("<meta http-equiv=\"content-type\" content=\"text/html;charset=" + _content_charset + "\"/>").append("\r\n");
			html.append("</head>").append("\r\n");
			html.append("<body style=\"text-align: center;margin:auto;width:1000px;\">").append("\r\n");
			
			BufferedReader br = null;
			try {
				
				br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), getFilecharset(txtFile)));
				String line = "";
				while((line = br.readLine()) != null){
					
					html.append("<span style=\"font-size: 14px;\">").append(line).append("</span>").append("</br>").append("\r\n");
				}
				
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
			}
			
			html.append("</body>").append("\r\n");
			html.append("</html>");
			
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(htmlFileDir);
				out.write(html.toString().getBytes(_content_charset));
				out.flush();
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	private String getFilecharset(File sourceFile) {
		
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		
		try {
			boolean checked = false;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1) {
				return charset; // 文件编码为 ANSI
			} else if (first3Bytes[0] == (byte) 0xFF
					&& first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE"; // 文件编码为 Unicode
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE
					&& first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE"; // 文件编码为 Unicode big endian
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF
					&& first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8"; // 文件编码为 UTF-8
				checked = true;
			}
			bis.reset();
			if (!checked) {
				int loc = 0;
				while ((read = bis.read()) != -1) {
					loc++;
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
							// (0x80
							// - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					} else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							} else
								break;
						} else
							break;
					}
				}
			}
			bis.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return charset;
	}
	
}
