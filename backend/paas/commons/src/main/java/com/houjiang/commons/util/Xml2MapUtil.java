package com.houjiang.commons.util;


import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @version 1.0
 * @date 创建时间：2019年3月6日 下午4:29:13
 * @author Amos
 */
public class Xml2MapUtil {

    /**
     * xml字符串转map
     * @param xml
     * @return
     */
    public static Map<String, Object> xml2map(String xml) throws DocumentException
    {
        try {
            // 增加对 XML 格式的初步检查
            if (xml == null || xml.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid XML string");
            }

            // 使用 SAXReader 替换 DocumentHelper.parseText() 以提高安全性
            SAXReader reader = new SAXReader();
            // 禁止 DTD
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(new StringReader(xml));

            Map<String, Object> map = new HashMap<>();
            if (doc == null) {
                return map;
            } else {
                // 获取根节点
                Element rootElement = doc.getRootElement();
                element2map(rootElement, map);
            }
            return map;
        } catch (DocumentException e) {
            // 记录异常信息
            System.err.println("Error parsing XML: " + e.getMessage());
            return new HashMap<>();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void element2map(Element elmt, Map<String, Object> map)
    {
        if (null == elmt)
        {
            return;
        }
        String name = elmt.getName();

        //判断当前节点的内容是否为文本（最里面一层节点）
        if (elmt.isTextOnly())
        {
            map.put(name, elmt.getText());
        }
        else
        {
            Map<String, Object> mapSub = new HashMap<>();

            //获取当前节点的所有子节点
            List<Element> elements = (List<Element>)elmt.elements();

            //利用递归获取节点值
            for (Element elmtSub : elements)
            {
                element2map(elmtSub, mapSub);
            }
            Object first = map.get(name);

            //判断
            if (null == first)
            {
                map.put(name, mapSub);
            }
            else
            {
                if (first instanceof List<?>)
                {
                    ((List<Map<String, Object>>)first).add(mapSub);
                }
                else
                {
                    List<Object> listSub = new ArrayList<Object>();
                    listSub.add(first);
                    listSub.add(mapSub);
                    map.put(name, listSub);
                }
            }
        }
    }

}