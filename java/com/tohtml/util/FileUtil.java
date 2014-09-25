package com.tohtml.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
/**
 *  文件操作
 * @author pengsheng
 *
 */
public class FileUtil {

	/**
	 *  复制文件
	 * @param fromFile : 源文件
	 * @param toFile : 目标文件
	 */
	public static void copyFile(File fromFile, File toFile) {

		FileInputStream from = null;
		FileOutputStream to = null;
		
		if (fromFile.isDirectory()) {
			toFile.mkdirs();
			return;
		}

		File parentfile = toFile.getParentFile();
		if (!parentfile.exists())
			parentfile.mkdirs();

		try {
			
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead);
			
		} catch (Exception e) {
		} finally {
			if (from != null) {
				try {
					from.close();
				} catch (IOException e) {
				}
			}
			if (to != null) {
				try {
					to.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 *  替换文件中内容
	 * @param filePath ： 文件路径
	 * @param src : 原内容
	 * @param std : 目标内容
	 * @param : 字符编码
	 */
	public static void replaceTextContent(String filePath, String src, String std) {
		replaceTextContent(filePath, src, std, "gb2312");
	}
	
	/**
	 *  替换文件中内容
	 * @param filePath ： 文件路径
	 * @param src : 原内容
	 * @param std : 目标内容
	 * @param : 字符编码
	 */
	public static void replaceTextContent(String filePath, String src, String std, String charset) {
		
		StringBuffer contentBuffer = new StringBuffer();
		File file = new File(filePath);
		if(file.exists()){
			
			BufferedReader br = null;
			FileOutputStream out = null;
			try {
				
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
				String line = "";
				while((line = br.readLine()) != null){
					line = line.replace(src, std);
					contentBuffer.append(line).append("\r\n");
				}
				
				out = new FileOutputStream(file);
				out.write(contentBuffer.toString().getBytes(charset));
				out.flush();
				
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
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
	 *  判断是否图片
	 * @param imageFile
	 * @return
	 */
	public static boolean isImage(File imageFile) {
		
		try {
			BufferedImage bi = ImageIO.read(imageFile);
			if (bi == null) {
				return false;
			}
			return true;
		} catch (IOException e) {
		} 
		return false;
	}

	/**
	 *  判断是否安装有Openoffice
	 * @return
	 */
	public static boolean isInstallOpenOffice() {

		String osName = System.getProperty("os.name");
		if (Pattern.matches("Linux.*", osName)) {
			return new File("/opt/openoffice.org3").exists();
		} else if (Pattern.matches("Windows.*", osName)) {
			String osarch = System.getProperty("os.arch");
			if (osarch.indexOf("64") > 0) {
				return new File("C:/Program Files (x86)/OpenOffice.org 3").exists();
			} else {
				return new File("C:/Program Files/OpenOffice.org 3").exists();
			}
		} else if (Pattern.matches("Mac.*", osName)) {
			return new File("/Applications/OpenOffice.org.app/Contents").exists();
		}
		return false;
	}
	
	/***
	 * 删除指定路径下所有文件
	 * @param path
	 * @return
	 */
	public static boolean delAllFile(String path) {
	       boolean flag = false;
	       File file = new File(path);
	       if (!file.exists()) {
	         return flag;
	       }
	       if (!file.isDirectory()) {
	         return flag;
	       }
	       String[] tempList = file.list();
	       File temp = null;
	       for (int i = 0; i < tempList.length; i++) {
	          if (path.endsWith(File.separator)) {
	             temp = new File(path + tempList[i]);
	          } else {
	              temp = new File(path + File.separator + tempList[i]);
	          }
	          if (temp.isFile()) {
	             temp.delete();
	          }
	          if (temp.isDirectory()) {
	             delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
	             delFolder(path + "/" + tempList[i]);//再删除空文件夹
	             flag = true;
	          }
	       }
	       return flag;
	     }
	
	 public static void delFolder(String folderPath) {
	     try {
	        delAllFile(folderPath); //删除完里面所有内容
	        String filePath = folderPath;
	        filePath = filePath.toString();
	        java.io.File myFilePath = new java.io.File(filePath);
	        myFilePath.delete(); //删除空文件夹
	     } catch (Exception e) {
	       e.printStackTrace(); 
	     }
	}
}
