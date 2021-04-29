package io.github.gnupinguin.analyzer.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@Component
@RequiredArgsConstructor
public class AutowiredBroadcastBeanPostProcessor implements BeanPostProcessor {

    private final ApplicationContext context;

    private final JavaSparkContext sc;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(AutowiredBroadcast.class)) {
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> typeOfBeanToInject = (Class<?>) genericType.getActualTypeArguments()[0];
                field.setAccessible(true);
                Object beanToInject = context.getBean(typeOfBeanToInject);
                ReflectionUtils.setField(field, bean, sc.broadcast(beanToInject));
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
