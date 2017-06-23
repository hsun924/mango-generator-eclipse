package com.hsun.mango.actions;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector;
import org.eclipse.jdt.ui.text.java.IInvocationContext;

/**
 * JDT工具类
 * 
 * @author lvhao
 *
 */
public class JDTUtils {

	/**
	 * 根据类型查找类名，返回其所有set方法调用字符串
	 * 
	 * @param context 调用上下文
	 * @param type 类名
	 * @return
	 * @throws JavaModelException
	 */
	public static SourceType getClassType(IInvocationContext context, String type) throws JavaModelException {
		// 以下是为了获取所选类型的类名，通过Person类型，去查找到类com.bean.Person
		CompilationUnit compilationUnit = context.getASTRoot();
		ImportRewrite importsRewrite = StubUtility.createImportRewrite(compilationUnit, false);

		// 类型
		char[][] allTypes = new char[1][];
		allTypes[0] = type.toCharArray();
		final ArrayList<TypeNameMatch> typesFound = new ArrayList<TypeNameMatch>();
		IPackageFragment fCurrPackage = (IPackageFragment) importsRewrite.getCompilationUnit().getParent();
		final IJavaProject project = fCurrPackage.getJavaProject();
		// 设置查找范围：在当前的Java Project中找
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		// 结果集
		TypeNameMatchCollector collector = new TypeNameMatchCollector(typesFound);
		// 调用查询引擎去查询类文件
		new SearchEngine().searchAllTypeNames(null, allTypes, scope, collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);

		if (typesFound.size() > 0) {
			// 取查找到的第一个类名
			JavaSearchTypeNameMatch typeNameMatch = (JavaSearchTypeNameMatch) typesFound.get(0);
			SourceType sourceType = (SourceType) typeNameMatch.getType();
			return sourceType;
		}
		return null;
	}
	/**
	 * 根据类型查找类名，返回其所有set方法调用字符串
	 * 
	 * @param compilationUnit
	 * @param type 类名
	 * @return
	 * @throws JavaModelException
	 */
	public static SourceType getClassType(ICompilationUnit icu, String type) throws JavaModelException {
		// 以下是为了获取所选类型的类名，通过Person类型，去查找到类com.bean.Person
		IPackageFragment fCurrPackage = (IPackageFragment) icu.getParent();
		// 类型
		char[][] allTypes = new char[1][];
		allTypes[0] = type.toCharArray();
		final ArrayList<TypeNameMatch> typesFound = new ArrayList<TypeNameMatch>();
		final IJavaProject project = fCurrPackage.getJavaProject();
		// 设置查找范围：在当前的Java Project中找
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		// 结果集
		TypeNameMatchCollector collector = new TypeNameMatchCollector(typesFound);
		// 调用查询引擎去查询类文件
		new SearchEngine().searchAllTypeNames(null, allTypes, scope, collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		
		if (typesFound.size() > 0) {
			// 取查找到的第一个类名
			JavaSearchTypeNameMatch typeNameMatch = (JavaSearchTypeNameMatch) typesFound.get(0);
			SourceType sourceType = (SourceType) typeNameMatch.getType();
			return sourceType;
		}
		return null;
	}

	/**
	 * 在指定类中过滤setter方法
	 * @param sourceTypes
	 * @return
	 */
	public static Vector<IMethod> filterSetterMethods(Vector<SourceType> sourceTypes) {
		Vector<IMethod> methods = new Vector<IMethod>();
		try {
			for (SourceType sourceType : sourceTypes) {
				ICompilationUnit compilationUnit = sourceType.getCompilationUnit();
				for (IType tempType : compilationUnit.getTypes()) {
					for (IMethod method : tempType.getMethods()) {
						if (!method.isConstructor()) {
							if (method.getElementName().startsWith("set")) {
								//子类有可能覆盖父类中的方法，在此判断，方法名不可重复
								if(!isMethodExisted(methods, method)){
									methods.add(method);
								}
							}
						}
					}
				}
			}

		} catch (JavaModelException e) {
			// ignore
		}
		return methods;
	}
	
	/**
	 * 判断方法是否已在集合中
	 * @param methods
	 * @param target
	 * @return
	 */
	public static boolean isMethodExisted(Vector<IMethod> methods, IMethod target) {
		for (IMethod method : methods) {
			if(method.getElementName().equals(target.getElementName())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 查询指定类的继承结构
	 * @param icu
	 * @param type
	 * @return
	 * @throws JavaModelException
	 */
	public static Vector<SourceType> findSourceTypeHierarchy(ICompilationUnit icu, String type) throws JavaModelException {
		Vector<SourceType> sourceTypes = new Vector<SourceType>();
		String className = type;
		while (className != null) {
			SourceType sourceType = getClassType(icu, className);
			if (sourceType == null) {
				break;
			}
			sourceTypes.add(sourceType);
			className = sourceType.getSuperclassName();
		}
		return sourceTypes;
	}
	/**
	 * 查询指定类的继承结构
	 * @param context
	 * @param type
	 * @return
	 * @throws JavaModelException
	 */
	public static Vector<SourceType> findSourceTypeHierarchy(IInvocationContext context, String type) throws JavaModelException {
		Vector<SourceType> sourceTypes = new Vector<SourceType>();
		String className = type;
		while (className != null) {
			SourceType sourceType = findSourceType(context, className);
			if (sourceType == null) {
				break;
			}
			sourceTypes.add(sourceType);
			className = sourceType.getSuperclassName();
		}
		return sourceTypes;
	}
	
	/**
	 * 查询指定类的结构
	 * @param context
	 * @param type
	 * @return
	 * @throws JavaModelException
	 */
	public static SourceType findSourceType(IInvocationContext context, String type) throws JavaModelException{
		SourceType sourceType = JDTUtils.getClassType(context, type);
		return sourceType;
	}
	
	
	/**
	 * 首字母小写
	 * @param str
	 * @return
	 */
	public static String firstCharacterToLower(String str){
		String first = str.substring(0, 1);
		if(str.length() > 1){
			return first.toLowerCase() + str.substring(1);
		}else{
			return first;
		}
	}
	
	/**
	 * 获取属性的JavaDoc注释
	 * @param field
	 * @return
	 */
	public static String getJavadocComment(IField field) {
		try {
			ISourceRange sr = field.getJavadocRange();
			if (null != sr) {
				String filedComment = field.getSource();
				filedComment = filedComment.substring(0, sr.getLength());
				filedComment = filedComment.replaceAll("[\n,\r,*,/, ,\t]", "");
				return filedComment;
			}
		} catch (JavaModelException e) {
		}
		return null;
	}
}
