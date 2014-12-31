package org.freeshr.application.fhir;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class Sample {
    public static interface DoSomething {
        public void execute();
        public void unDo();
    }

    public static class SayHello implements DoSomething {
        @Override
        public void execute() {
            System.out.println("Hello...");
        }

        @Override
        public void unDo() {
            System.out.println("Undo ...");
        }


    }

    public static void main(String[] args) {
        DoSomething hello = new SayHello();
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName() == "execute") {
                    System.out.println("Hello from proxy");
                }
                return null;
            }
        };
        DoSomething proxy = (DoSomething) Proxy.newProxyInstance(DoSomething.class.getClassLoader(), new Class[]{DoSomething.class}, handler);
        proxy.execute();
        proxy.unDo();
    }

}
