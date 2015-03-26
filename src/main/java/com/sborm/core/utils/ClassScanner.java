package com.sborm.core.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 感觉暂时用不上了
 * 
 * @author Rick
 * @date 2015-3-16 下午11:16:14 
 *
 */
@Deprecated
public class ClassScanner {

	/**
	 * 获取所有类名
	 * 
	 * @param packageName
	 * @return
	 */
	// public static List<String> getClassName(String packageName) {
	// List<String> classNames = new ArrayList<String>();
	// try {
	// if (!packageName.endsWith(".")) {
	// packageName += ".";
	// }
	// String resourceName = packageName.replaceAll(".", "/");
	// URL url =
	// Thread.currentThread().getContextClassLoader().getResource(resourceName);
	// File urlFile = new File(url.getPath());
	// File[] files = urlFile.listFiles();
	// for (File f : files)
	// getClassName(packageName, f, classNames);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return classNames;
	// }

	public static List<String> getClassName(String packageName) {
		List<String> fileNames = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String packagePath = packageName.replace(".", "/");
		URL url = loader.getResource(packagePath);
		if (url != null) {
			String type = url.getProtocol();
			if (type.equals("file")) {
				fileNames = getClassNameByFile(url.getPath(), null,
						false);
			} else if (type.equals("jar")) {
				fileNames = getClassNameByJar(url.getPath(), true);
			}
		} else {
			fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(),
					packagePath, true);
		}
		return fileNames;
	}

	/**
	 * 递归扫描
	 * 
	 * @param packageName
	 * @param packageFile
	 * @param list
	 */
	private static void getClassName(String packageName, File packageFile,
			List<String> list) {
		if (packageFile.isFile()) {
			if (packageFile.getName().endsWith(".class")) {
				list.add(packageName + "."
						+ packageFile.getName().replace(".class", ""));
			}
		} else {
			File[] files = packageFile.listFiles();
			String tmPackageName = "";
			if (packageName == null || packageName.length() == 0
					|| packageName.endsWith(".")) {
				tmPackageName = packageName + packageFile.getName();
			} else {
				tmPackageName = packageName + "." + packageFile.getName();
			}
			for (File f : files) {
				getClassName(tmPackageName, f, list);
			}
		}
	}

	/**
	 * 从项目文件获取某包下所有类
	 * 
	 * @param filePath
	 *            文件路径
	 * @param className
	 *            类名集合
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	private static List<String> getClassNameByFile(String filePath,
			List<String> className, boolean childPackage) {
		List<String> myClassName = new ArrayList<String>();
		File file = new File(filePath);
		File[] childFiles = file.listFiles();
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				if (childPackage) {
					myClassName.addAll(getClassNameByFile(childFile.getPath(),
							myClassName, childPackage));
				}
			} else {
				String childFilePath = childFile.getPath();
				if (childFilePath.endsWith(".class")) {
					childFilePath = childFilePath.substring(
							childFilePath.indexOf("\\classes") + 9,
							childFilePath.lastIndexOf("."));
					childFilePath = childFilePath.replace("\\", ".");
					myClassName.add(childFilePath);
				}
			}
		}

		return myClassName;
	}

	/**
	 * 从jar获取某包下所有类
	 * 
	 * @param jarPath
	 *            jar文件路径
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	private static List<String> getClassNameByJar(String jarPath,
			boolean childPackage) {
		List<String> myClassName = new ArrayList<String>();
		String[] jarInfo = jarPath.split("!");
		String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
		String packagePath = jarInfo[1].substring(1);
		try {
			JarFile jarFile = new JarFile(jarFilePath);
			Enumeration<JarEntry> entrys = jarFile.entries();
			while (entrys.hasMoreElements()) {
				JarEntry jarEntry = entrys.nextElement();
				String entryName = jarEntry.getName();
				if (entryName.endsWith(".class")) {
					if (childPackage) {
						if (entryName.startsWith(packagePath)) {
							entryName = entryName.replace("/", ".").substring(
									0, entryName.lastIndexOf("."));
							myClassName.add(entryName);
						}
					} else {
						int index = entryName.lastIndexOf("/");
						String myPackagePath;
						if (index != -1) {
							myPackagePath = entryName.substring(0, index);
						} else {
							myPackagePath = entryName;
						}
						if (myPackagePath.equals(packagePath)) {
							entryName = entryName.replace("/", ".").substring(
									0, entryName.lastIndexOf("."));
							myClassName.add(entryName);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return myClassName;
	}

	/**
	 * 从所有jar中搜索该包，并获取该包下所有类
	 * 
	 * @param urls
	 *            URL集合
	 * @param packagePath
	 *            包路径
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	private static List<String> getClassNameByJars(URL[] urls,
			String packagePath, boolean childPackage) {
		List<String> myClassName = new ArrayList<String>();
		if (urls != null) {
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				String urlPath = url.getPath();
				// 不必搜索classes文件夹
				if (urlPath.endsWith("classes/")) {
					continue;
				}
				String jarPath = urlPath + "!/" + packagePath;
				myClassName.addAll(getClassNameByJar(jarPath, childPackage));
			}
		}
		return myClassName;
	}

	public static void main(String[] args) {
		getClassName("");
	}
}