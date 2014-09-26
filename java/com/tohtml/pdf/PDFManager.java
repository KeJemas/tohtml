package com.tohtml.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 *  将PDF转换为图片,再将图片添加到Html文件中
 * @author pengsheng
 *
 */
public class PDFManager {
	
	private static Log logger = LogFactory.getLog(PDFManager.class);
	
	private static String _content_charset = "gb2312";
	
	public PDFManager() {
	}
	
	/**
	 *  将PDF转换为Html
	 * @param pdfFileDir : PDF文件路径
	 * @return 返回包含有Html和image的文件夹路径
	 */
	public String conveterPDF2Html(String pdfFileDir) {
		return conveterPDF2Html(pdfFileDir, "");
	}
	
	/**
	 *  将PDF转换为Html
	 * @param pdfFileDir : PDF文件路径
	 * @param outDir : 输出文件夹
	 * @return 返回包含有Html和image的文件夹路径
	 */
	public String conveterPDF2Html(String pdfFileDir, String outDir) {
		
		String htmlDir = "";
		String uuid = "";
		
		File pdfFile = new File(pdfFileDir);
		if (pdfFile.exists()) {
			
			if ("".equals(outDir)) {
				uuid = UUID.randomUUID().toString().replace("-", "");
				htmlDir = pdfFile.getParentFile().getAbsolutePath() + File.separator + uuid + File.separator;
			} else {
				uuid = new File(outDir).getName();
				htmlDir = outDir + File.separator + uuid + File.separator;
			}
			
			new File(htmlDir).mkdir();

			List<String> imageList = conveterPDF2Image(pdfFile, htmlDir);

			createHtml(imageList, htmlDir + uuid + ".html");
			
		} 

		return htmlDir;
	}
	
	/**
	 *  生成Html文件
	 * @param imageList : 图片路径列表
	 * @param htmlFileDir : Html文件路径
	 */
	private void createHtml(List<String> imageList, String htmlFileDir) {
		
		if (imageList.size() > 0) {
			
			StringBuffer html = new StringBuffer();
			html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">").append("\r\n");
			html.append("<html>").append("\r\n");
			html.append("<head>").append("\r\n");
			//html.append("<title>").append(fileName).append("<title>");
			html.append("<meta http-equiv=\"content-type\" content=\"text/html;charset=" + _content_charset + "\"/>").append("\r\n");
			html.append("</head>").append("\r\n");
			html.append("<body style=\"text-align: center;margin:auto;width:1000px;\">").append("\r\n");
			
			for (String imageDir : imageList) {
				
				try {
					
					File imageFile = new File(imageDir);
					BufferedImage imd = ImageIO.read(imageFile);
					String imageName = imageFile.getName();
					
					int heigth = imd.getHeight();
		            int width = imd.getWidth();
                    if (width > 1000) {

                        int scale = width / 1000;
                        width = 1000;
                        heigth = heigth / scale;
                    }
		            
                    
                    html.append("<img src='" + imageName + "' width='" + width + "px' height='" + heigth + "px' border='0'/>").append("\r\n");
                    
				} catch (IOException e) {
					logger.error(e);
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
	
	/**
	 *  将PDF转换为图片
	 * @param pdfFile : PDF文件
	 * @param destDir : 输出路径
	 * @return 图片路径列表
	 */
	private List<String> conveterPDF2Image(File pdfFile, String destDir) {
		
		List<String> imageList = new ArrayList<String>();
		PDDocument document = null;
		try {

			File destFile = new File(destDir);
			if (!destFile.exists()) {
				destFile.mkdir();
			}

			document = PDDocument.load(pdfFile.getAbsolutePath());
			List<PDPage> list = document.getDocumentCatalog().getAllPages();
			int pageNumber = 1;
			
			for (PDPage page : list) {
				
				BufferedImage image = page.convertToImage();
				File outputfile = new File(destDir + "_" + pageNumber + ".png");
				ImageIO.write(image, "png", outputfile);
				
				imageList.add(destDir + "_" + pageNumber + ".png");
				
				pageNumber++;
			}
			
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (document != null)
					document.close();
			} catch (IOException e) {
			}
		}
		
		return imageList;
	}
}
