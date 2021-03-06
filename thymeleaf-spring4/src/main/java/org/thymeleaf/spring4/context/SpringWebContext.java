/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.spring4.context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.WebContext;

/**
 * <p>
 *   Implementation of {@link org.thymeleaf.context.IContext} meant for Spring MVC applications,
 *   extending {@link WebContext} and adding:
 * </p>
 * <ul>
 *   <li>A special <tt>beans</tt> variable of class ({@link Beans}) that allows users to access beans 
 *       in the application context. This variable can be accessed like any other variable
 *       in the context: <tt>${beans.myBean.doSomething()}</tt>.</li>
 *   <li>A reference to the Application Context itself, that can be obtained and used from
 *       element/attribute processors ({@link #getApplicationContext()}).</li>
 * </ul>
 * 
 * @author Daniel Fern&aacute;ndez
 * @author Josh Long
 * 
 * @since 1.0
 *
 */
public class SpringWebContext 
        extends WebContext {
    

    public static final String BEANS_VARIABLE_NAME = "beans";
    private static final ConcurrentHashMap<ApplicationContext, HashMap<String, Object>> variableMapPrototypes =
            new ConcurrentHashMap<ApplicationContext, HashMap<String, Object>>();
    
    private final ApplicationContext applicationContext;



    /**
     * <p>
     *   Creates a new instance of a SpringWebContext.
     * </p>
     * 
     * @param request the request object
     * @param response the response object
     * @param servletContext the servlet context
     * @param locale the locale
     * @param variables the variables to be included into the context
     * @param appctx the Spring application context
     */
    public SpringWebContext(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final ServletContext servletContext ,
                            final Locale locale, 
                            final Map<String, ?> variables, 
                            final ApplicationContext appctx) {
        super(request, response, servletContext, locale, addSpringSpecificVariables(variables, appctx));
        this.applicationContext = appctx;
    }

    
    

    @SuppressWarnings("unchecked")
    private static Map<String,Object> addSpringSpecificVariables(final Map<String, ?> variables, final ApplicationContext appctx) {

        HashMap<String,Object> variableMapPrototype = variableMapPrototypes.get(appctx);
        if (variableMapPrototype == null) {
            variableMapPrototype = new HashMap<String, Object>(20, 1.0f);
            // We will use a singleton-per-appctx Beans instance, and that's alright
            final Beans beans = new Beans(appctx);
            variableMapPrototype.put(BEANS_VARIABLE_NAME, beans);
            variableMapPrototypes.put(appctx, variableMapPrototype);
        }

        final Map<String,Object> newVariables;
        synchronized (variableMapPrototype) {
            newVariables = (Map<String, Object>) variableMapPrototype.clone();
        }

        if (variables != null) {
            newVariables.putAll(variables);
        }

        return newVariables;
        
    }


    
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }
    
    
}
