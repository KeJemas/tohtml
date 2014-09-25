package com.tohtml.test;

import java.io.IOException;

import com.tohtml.office.OfficeDocumentManager;

public class Test {

	public static void main(String[] args) throws IOException {

		for (int i = 0; i < 100; i++) {

			new Thread(new Runnable() {
				
				public void run() {

					for (int j = 0; j < 10; j++) {
						
						try {
							OfficeDocumentManager.getInstance().conveterOfficeDocument2Html("E:/ABC/A.doc");
						} catch (IOException e) {
						}
					}
				}
				
			}).start();
		}

	}

}
