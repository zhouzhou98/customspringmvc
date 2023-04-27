package com.custom.springmvc.servlet;

import com.custom.springmvc.annotation.Controller;
import com.custom.springmvc.annotation.RequestMapping;
import com.custom.springmvc.annotation.ResponseBody;
import com.custom.springmvc.context.WebApplicationContext;
import com.custom.springmvc.handler.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
public class DispatcherServlet extends HttpServlet {
    private WebApplicationContext webApplicationContext;
    private List<RequestHandler> handlerList = new ArrayList<>();
    @Override
    public void init() throws ServletException {

        //1、加载初始化参数   classpath:springmvc.xml
        String  contextConfigLocation =  this.getServletConfig().getInitParameter("contextConfigLocation");

        //2、创建Springmvc容器
        webApplicationContext = new WebApplicationContext(contextConfigLocation);

        //3、进行初始化操作
        webApplicationContext.onRefresh();

        //4、初始化请求映射关系   /findUser   ===》控制器.方法
        initHandlerMapping();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //进行请求分发处理
        doDispatcher(request,response);
    }

    public void  doDispatcher(HttpServletRequest req, HttpServletResponse resp){

        //根据用户的请求地址  /findUser   查找Handler|Controller
        RequestHandler requestHandler = getHandler(req);

        try{

            if(requestHandler == null){
                resp.getWriter().print("<h1>404 NOT  FOUND!</h1>");
            }else{

                //调用处理方法之前 进行参数的注入

                //调用目标方法
                Object result = requestHandler.getMethod().invoke(requestHandler.getController());

                if(result instanceof String){
                    //跳转JSP
                    String viewName=(String)result;
                    // forward:/success.jsp
                    if(viewName.contains(":")){
                        String viewType=viewName.split(":")[0];
                        String viewPage=viewName.split(":")[1];
                        if(viewType.equals("forward")){
                            req.getRequestDispatcher(viewPage).forward(req,resp);
                        }else{
                            // redirect:/user.jsp
                            resp.sendRedirect(viewPage);
                        }
                    }else{
                        //默认就转发
                        req.getRequestDispatcher(viewName).forward(req,resp);
                    }
                }else{
                    //返回JSON格式数据
                    Method method = requestHandler.getMethod();
                    if(method.isAnnotationPresent(ResponseBody.class)){
                        //将返回值转换成 json格式数据
                        ObjectMapper objectMapper = new ObjectMapper();
                        String json = objectMapper.writeValueAsString(result);
                        resp.setContentType("text/html;charset=utf-8");
                        PrintWriter writer = resp.getWriter();
                        writer.print(json);
                        writer.flush();
                        writer.close();

                    }
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }


    }

    private RequestHandler getHandler(HttpServletRequest req) {
        // /findUser
        String requestURI = req.getRequestURI();
        for (RequestHandler myHandler : this.handlerList) {
            //从容器的Handle取出URL  和  用户的请求地址进行匹配，找到满足条件的Handler
            if (myHandler.getUrl().equals(requestURI)) {
                return myHandler;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request,response);
    }

    //初始化请求映射关系
    public void  initHandlerMapping(){

        for (Entry<String, Object> entry : webApplicationContext.getIocMap().entrySet()) {
            //获取bean的class类型
            Class<?> clazz =  entry.getValue().getClass();

            if(clazz.isAnnotationPresent(Controller.class)){
                //获取bean中所有的方法，为这些方法建立映射关系
                Method[] methods =  clazz.getDeclaredMethods();
                for (Method method : methods) {

                    if(method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        //获取注解中的值   /findUser
                        String url = requestMapping.value();

                        //建立  映射地址  与  控制器.方法
                        RequestHandler requestHandler = new RequestHandler(url,entry.getValue(),method);
                        handlerList.add(requestHandler);
                    }
                }

            }
        }

    }
}
