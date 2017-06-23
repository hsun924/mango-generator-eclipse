package com.hsun.mango.actions;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class SourceGenerator {
    private static Configuration config = null;

    static{
        config = new Configuration();
        config.setClassForTemplateLoading(SourceGenerator.class, "/ftl");
        config.setDefaultEncoding("UTF-8");
    }

    public static void generate(String template, Object data, String target) throws IOException, TemplateException {
        Template tp = config.getTemplate(template);

        File file = new File(target);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        tp.setEncoding("UTF-8");
        tp.process(data, out);
        out.flush();
        out.close();
    }
}
