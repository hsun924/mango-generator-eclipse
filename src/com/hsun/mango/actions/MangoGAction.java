package com.hsun.mango.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.PluginAction;

import freemarker.template.TemplateException;

public class MangoGAction implements IObjectActionDelegate {

	private Shell shell;
	
	/**
	 * Constructor for Action1.
	 */
	public MangoGAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		PluginAction opAction = (PluginAction)action;
		ISelection selection = opAction.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof ICompilationUnit) {
				try {
					ICompilationUnit icu = (ICompilationUnit) firstElement;
					String bean = icu.getElementName();
					bean = bean.substring(0, bean.lastIndexOf("."));
					IPackageDeclaration[] packageDeclarations = null;
						packageDeclarations = icu.getPackageDeclarations();
					String basePackage = packageDeclarations[0].getElementName();
					basePackage = basePackage.substring(0, basePackage.lastIndexOf("."));
					Map<String, Object> map = new HashMap<>();
					map.put("bean", bean);
					map.put("table", "t" + c2_(bean));
					map.put("basePackage", basePackage);
					final Map<String, String> results = new LinkedHashMap<>();
					map.put("results", results);
					ASTParser parse = ASTParser.newParser(3);
					parse.setSource(icu);
					parse.setResolveBindings(true);
					CompilationUnit cu  = (CompilationUnit) parse.createAST(null);
					final List<String> types = new ArrayList<String>();
					
					cu.accept(new ASTVisitor() {
						int count = 0;
						@Override
						public boolean visit(FieldAccess node) {
							String cname = node.getName().toString();
							results.put(cname, p(types.get(count), cname));
							count++;
							return super.visit(node);
						}
						
						@Override
						public boolean visit(FieldDeclaration node) {
							types.add(node.getType().toString());
							return super.visit(node);
						}
						
					});
					
					IJavaProject javaProject = icu.getJavaProject();
					IProject project = javaProject.getProject();
					
					IPath path = icu.getPath();
					IPath projectPath = javaProject.getPath();
					
					String replace = path.toString().replace(projectPath.toString(), "");
					
					Path p = new Path(replace);
					
				    IFile iFile = project.getFile(p);
				    IPath location = iFile.getLocation();
				    File file = location.toFile();
					File parentFile = file.getParentFile().getParentFile();
					String target = parentFile.getPath();
				    
					SourceGenerator.generate("dao.ftl", map, target + "/dao/I" + bean + "Dao.java");
					SourceGenerator.generate("service.ftl", map, target + "/service/I" + bean + "Service.java");
					SourceGenerator.generate("serviceImpl.ftl", map, target + "/service/impl/" + bean + "ServiceImpl.java");
					SourceGenerator.generate("control.ftl", map, target + "/control/" + bean + "Control.java");
					SourceGenerator.generate("sql.ftl", map, target + "/dao/" + bean + "-craete.sql");
					
					MessageDialog.openInformation(
							shell,
							"MangoG",
							"代码生成成功");
					
				} catch (JavaModelException e) {
					MessageDialog.openInformation(
							shell,
							"mango-generator-eclipse",
							e.getMessage());
				} catch (IOException e) {
					MessageDialog.openInformation(
							shell,
							"mango-generator-eclipse",
							e.getMessage());
				} catch (TemplateException e) {
					MessageDialog.openInformation(
							shell,
							"mango-generator-eclipse",
							e.getMessage());
				} catch (Exception e) {
					MessageDialog.openInformation(
							shell,
							"mango-generator-eclipse",
							e.getMessage());
				}
			}
		}
		
		
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	
	}
	
    private static String p(String type, String name) {
        switch (type) {
            case "Long":
            case "long":
                return "n_" + c2_(name);
            case "Integer":
            case "int":
            case "Double":
            case "double":
            case "Float":
            case "float":
            case "Short":
            case "short":
            case "Boolean":
            case "boolean":
                return "n_" + c2_(name);
            case "String":
                return "c_" + c2_(name);
            default:
                return "c_" + c2_(name);
        }
    }
    
    private static String c2_(String str){
        if (null == str || str.equals("")) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++ ) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
