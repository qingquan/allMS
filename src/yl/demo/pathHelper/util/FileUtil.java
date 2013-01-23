/**
 * FileUtil.java 	Version <1.00>	2012-8-18
 *
 * Copyright(C) 2009-2012  All rights reserved. 
 * Lu Zhiyong is a student majoring in Software Engineering (Communication Software), 
 * from the School of Software, SUN YAT-SEN UNIVERSITY, GZ 510006, P. R. China.
 *
 * Blog: http://www.gzayong.info
 */
package yl.demo.pathHelper.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

/**
 * @author YONG
 *
 */
public class FileUtil {
	public static boolean initDatabase(Context context, String assetsFileName) {
		File databaseFolder = new File(context.getFilesDir().getParentFile().getAbsolutePath() + "/databases/");
		if(!databaseFolder.exists() || !databaseFolder.isDirectory()) {
			databaseFolder.mkdirs();
		}
		try {
			InputStream inputStream = context.getAssets().open(assetsFileName);
			FileOutputStream output = new FileOutputStream(databaseFolder.getAbsolutePath() + "/" + assetsFileName);
			byte[] buf = new byte[10240];
			int count = 0;
			while ((count = inputStream.read(buf)) > 0) {
				output.write(buf, 0, count);
			}
			output.close();
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static Bitmap getBitmapFromRes(Context context, int id) {
		InputStream is = context.getResources().openRawResource(id);
	     BitmapFactory.Options options=new BitmapFactory.Options();
	     options.inJustDecodeBounds = false;
	     options.inSampleSize = 1;   
	     Bitmap btp =BitmapFactory.decodeStream(is,null,options);
	     return btp;
	}
}
