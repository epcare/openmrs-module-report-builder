package org.openmrs.module.reportbuilder.legacyconfig.resolver;

import org.openmrs.api.context.Context;
import org.openmrs.module.reportbuilder.legacyconfig.model.AliasMethodConfig;
import org.openmrs.module.reportbuilder.legacyconfig.model.FactoryMethodConfig;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class SpringBeanMethodInvoker {
	
	private final ApplicationContext applicationContext;
	
	public SpringBeanMethodInvoker(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public Object invoke(AliasMethodConfig config) {
		Object[] args = config.getArguments() == null ? new Object[0] : config.getArguments().toArray(new Object[0]);
		return invoke(config.getBean(), config.getBeanClass(), config.getMethod(), args);
	}
	
	public Object invoke(FactoryMethodConfig config, Object... args) {
		return invoke(config.getBean(), config.getBeanClass(), config.getMethod(), args);
	}
	
	public Object invoke(String beanName, String methodName) {
		return invoke(beanName, null, methodName, new Object[0]);
	}
	
	public Object invoke(String beanName, String methodName, Object... args) {
		return invoke(beanName, null, methodName, args);
	}
	
	public Object invoke(String beanName, String beanClassName, String methodName, Object... args) {
		if ((beanName == null || beanName.trim().length() == 0)
		        && (beanClassName == null || beanClassName.trim().length() == 0)) {
			throw new IllegalArgumentException("Bean name or beanClass is required");
		}
		if (methodName == null || methodName.trim().length() == 0) {
			throw new IllegalArgumentException("Method name is required");
		}
		Object bean = resolveBean(beanName, beanClassName);
		Method method = findMethod(bean.getClass(), methodName, args);
		if (method == null) {
			throw new IllegalArgumentException("No matching method '" + methodName + "' found on target '"
			        + bean.getClass().getName() + "'");
		}
		try {
			method.setAccessible(true);
			return method.invoke(bean, args);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed invoking method: " + bean.getClass().getName() + "." + methodName, e);
		}
	}
	
	private Object resolveBean(String beanName, String beanClassName) {
		if (hasText(beanName)) {
			try {
				return applicationContext.getBean(beanName);
			}
			catch (NoSuchBeanDefinitionException ignore) {
				// fall through to class-based resolution
			}
		}
		Class<?> beanClass = null;
		if (hasText(beanClassName)) {
			beanClass = loadClass(beanClassName);
		} else if (hasText(beanName) && beanName.contains(".")) {
			beanClass = loadClass(beanName);
		}
		if (beanClass != null) {
			Object byType = resolveBeanByType(beanName, beanClass);
			if (byType != null) {
				return byType;
			}
		}
		throw new IllegalArgumentException("Unable to resolve bean. name='" + beanName + "', beanClass='" + beanClassName
		        + "'");
	}
	
	private Object resolveBeanByType(String beanName, Class<?> beanClass) {
		try {
			return applicationContext.getBean(beanClass);
		}
		catch (Exception ignore) {
			// continue
		}
		try {
			Map<?, ?> beans = applicationContext.getBeansOfType(beanClass);
			if (beans != null && !beans.isEmpty()) {
				if (hasText(beanName) && beans.containsKey(beanName)) {
					return beans.get(beanName);
				}
				return beans.values().iterator().next();
			}
		}
		catch (Exception ignore) {
			// continue
		}
		try {
			String registeredName = hasText(beanName) ? beanName : decapitalize(beanClass.getSimpleName());
			Object registered = Context.getRegisteredComponent(registeredName, (Class) beanClass);
			if (registered != null) {
				return registered;
			}
		}
		catch (Exception ignore) {
			// continue
		}
		return null;
	}
	
	private Method findMethod(Class<?> type, String methodName, Object[] args) {
		Method[] methods = type.getMethods();
		int i;
		for (i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (!method.getName().equals(methodName)) {
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != args.length) {
				continue;
			}
			if (matches(parameterTypes, args)) {
				return method;
			}
		}
		return null;
	}
	
	private boolean matches(Class<?>[] parameterTypes, Object[] args) {
		int i;
		for (i = 0; i < parameterTypes.length; i++) {
			if (!isCompatible(parameterTypes[i], args[i])) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isCompatible(Class<?> parameterType, Object arg) {
		if (arg == null) {
			return !parameterType.isPrimitive();
		}
		Class<?> argumentType = arg.getClass();
		if (parameterType.isPrimitive()) {
			parameterType = primitiveToWrapper(parameterType);
		}
		return parameterType.isAssignableFrom(argumentType);
	}
	
	private Class<?> primitiveToWrapper(Class<?> primitive) {
		if (Boolean.TYPE.equals(primitive)) {
			return Boolean.class;
		}
		if (Integer.TYPE.equals(primitive)) {
			return Integer.class;
		}
		if (Long.TYPE.equals(primitive)) {
			return Long.class;
		}
		if (Double.TYPE.equals(primitive)) {
			return Double.class;
		}
		if (Float.TYPE.equals(primitive)) {
			return Float.class;
		}
		if (Short.TYPE.equals(primitive)) {
			return Short.class;
		}
		if (Byte.TYPE.equals(primitive)) {
			return Byte.class;
		}
		if (Character.TYPE.equals(primitive)) {
			return Character.class;
		}
		return primitive;
	}
	
	private Class<?> loadClass(String className) {
		try {
			return Class.forName(className);
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to load class: " + className, e);
		}
	}
	
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}
	
	private String decapitalize(String value) {
		if (!hasText(value)) {
			return value;
		}
		if (value.length() == 1) {
			return value.toLowerCase();
		}
		return Character.toLowerCase(value.charAt(0)) + value.substring(1);
	}
}
