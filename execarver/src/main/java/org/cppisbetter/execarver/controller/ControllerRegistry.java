package org.cppisbetter.execarver.controller;

import org.cppisbetter.execarver.carver.BaseCarver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

///
/// PURPOSE
///     Associate a view with a controller and FXML file
///
public class ControllerRegistry {

    private static /* internal */ class ViewData {
        private final String m_viewFXMLPath;
        private final Class<?> m_class;
        private final BaseCarver m_carver;

        public ViewData(String fxmlPath, Class<?> klass, BaseCarver carver) {
            m_viewFXMLPath = fxmlPath;
            m_class = klass;
            m_carver = carver;
        }
    }

    /// Map View names to view data
    private HashMap<String, ViewData> m_viewMap;

    public ControllerRegistry() {
        m_viewMap = new HashMap<>();
    }

    public void register(String name, String path, Class<?> klass, BaseCarver carver) {
        m_viewMap.put(name, new ViewData(path, klass, carver));
    }

    // https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Constructor.html
    public Object instantiate(String name) {
        if (m_viewMap.containsKey(name)) {
            ViewData vd = m_viewMap.get(name);
            try {
                Constructor<?> ctor = vd.m_class.getConstructor(BaseCarver.class);
                Object instance = ctor.newInstance(vd.m_carver);

                return instance;

            } catch(NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

        return null;
    }



}
