package com.tohtml.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tohtml.util.FileUtil;



/**
 * 将图片放在Html中
 * @author pengsheng
 *
 */
public class ImageManager {

	private static Log logger = LogFactory.getLog(ImageManager.class);
	
	private static String _content_charset = "gb2312";
	
	public ImageManager() {
	}
	
	/**
	 *  
	 * @param imageDir : 图片路径
	 * @return 包含有图片和Html的文件夹
	 */
	public String conveterImage2Html(String imageDir) {
		return conveterImage2Html(imageDir, "");
	}
	
	/**
	 *  
	 * @param imageDir : 图片路径
	 * @param outDir : 输出文件夹
	 * @return 包含有图片和Html的文件夹
	 */
	public String conveterImage2Html(String imageDir, String outDir) {
		
		String htmlDir = "";
		String uuid = "";
		File imageFile = new File(imageDir);
		if (imageFile.exists()) {
			
			if ("".equals(outDir)) {
				
				uuid = UUID.randomUUID().toString().replace("-", "");
				htmlDir = imageFile.getParentFile().getAbsolutePath() + File.separator + uuid + File.separator;
			} else {
				
				uuid = new File(outDir).getName();
				htmlDir = outDir + File.separator + uuid + File.separator;
			}
			
			new File(htmlDir).mkdir();

			String imageType = FilenameUtils.getExtension(imageFile.getName());

			File toFile = new File(htmlDir + uuid + "." + imageType);
			FileUtil.copyFile(imageFile, toFile);

			createHtml(toFile, htmlDir + uuid + ".html");
		}
		
		return htmlDir;
	}
	
	private void createHtml(File imageFile, String htmlFileDir) {
		
		
		StringBuffer html = new StringBuffer();
		html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">").append("\r\n");
		html.append("<html>").append("\r\n");
		html.append("<head>").append("\r\n");
		html.append("<meta http-equiv=\"content-type\" content=\"text/html;charset=" + _content_charset + "\"/>").append("\r\n");
		html.append("</head>").append("\r\n");
		html.append("<body style=\"text-align: center;margin:auto;width:1000px;\">").append("\r\n");
		
		try {
			BufferedImage imd = ImageIO.read(imageFile);
			String imageName = imageFile.getName();
			
			int heigth = imd.getHeight();
            int width = imd.getWidth();
            if (width > 1000) {
            	
            	int scale = heigth / 1000;
            	heigth = 1000;
            	width = width / scale;
            }
            
            html.append("<img src='" + imageName + "' width='" + width + "px' height='px" + heigth + "' border='0'/>").append("\r\n");
			
		} catch (IOException e) {
			logger.error(e);
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
