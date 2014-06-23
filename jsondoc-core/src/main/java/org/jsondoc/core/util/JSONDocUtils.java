package org.jsondoc.core.util;

import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.*;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class JSONDocUtils {

	public static final String      UNDEFINED   = "undefined";

	public static final String      WILDCARD    = "wildcard";

	private static      Reflections reflections = null;

	private static Logger log = LoggerFactory.getLogger(JSONDocUtils.class);

	/**
	 * Returns the main <code>ApiDoc</code>, containing <code>ApiMethodDoc</code> and
	 * <code>ApiObjectDoc</code> objects
	 *
	 * @return An <code>ApiDoc</code> object
	 */
	public static JSONDoc getApiDoc(String version, String basePath, List<String> packages) {

		Set<URL> urls = new HashSet<URL>();
		FilterBuilder filter = new FilterBuilder();

		log.debug("Found " + packages.size() + " package(s) to scan...");
		for(String pkg : packages) {
			log.debug("Adding package to JSONDoc recursive scan: " + pkg);
			urls.addAll(ClasspathHelper.forPackage(pkg));
			filter.includePackage(pkg);
		}

		reflections = new Reflections(new ConfigurationBuilder()
				.filterInputsBy(filter)
				.setUrls(urls)
		);

		JSONDoc apiDoc = new JSONDoc(version, basePath);
		apiDoc.setApis(getApiDocs(reflections.getTypesAnnotatedWith(Api.class)));
		apiDoc.setObjects(getApiObjectDocs(reflections.getTypesAnnotatedWith(ApiObject.class)));
		return apiDoc;
	}

	public static Set<ApiDoc> getApiDocs(Set<Class<?>> classes) {

		Set<ApiDoc> apiDocs = new TreeSet<ApiDoc>();
		for(Class<?> controller : classes) {
			log.debug("Getting JSONDoc for class: " + controller.getName());
			ApiDoc apiDoc = ApiDoc.buildFromAnnotation(controller.getAnnotation(Api.class));
			apiDoc.setMethods(getApiMethodDocs(controller));
			apiDocs.add(apiDoc);
		}
		return apiDocs;
	}

	public static Set<ApiObjectDoc> getApiObjectDocs(Set<Class<?>> classes) {

		Set<ApiObjectDoc> pojoDocs = new TreeSet<ApiObjectDoc>();
		for(Class<?> pojo : classes) {
			log.debug("Getting JSONDoc for class: " + pojo.getName());
			ApiObject annotation = pojo.getAnnotation(ApiObject.class);
			ApiObjectDoc pojoDoc = ApiObjectDoc.buildFromAnnotation(annotation, pojo);
			if(annotation.show()) {
				pojoDocs.add(pojoDoc);
			}
		}
		return pojoDocs;
	}

	private static List<ApiMethodDoc> getApiMethodDocs(Class<?> controller) {

		List<ApiMethodDoc> apiMethodDocs = new ArrayList<ApiMethodDoc>();
		Method[] methods = controller.getMethods();
		for(Method method : methods) {
			if(method.isAnnotationPresent(ApiMethod.class)) {
				ApiMethodDoc apiMethodDoc
						= ApiMethodDoc.buildFromAnnotation(method.getAnnotation(ApiMethod.class));

				if(method.isAnnotationPresent(ApiHeaders.class)) {
					apiMethodDoc.setHeaders(ApiHeaderDoc.buildFromAnnotation(method.getAnnotation
							(ApiHeaders.class)));
				}

				apiMethodDoc.setPathparameters(ApiParamDoc.getApiParamDocs(method,
						ApiParamType.PATH));

				apiMethodDoc.setQueryparameters(ApiParamDoc.getApiParamDocs(method,
						ApiParamType.QUERY));

				apiMethodDoc.setBodyobject(ApiBodyObjectDoc.buildFromAnnotation(method));

				if(method.isAnnotationPresent(ApiResponseObject.class)) {
					apiMethodDoc.setResponse(ApiResponseObjectDoc.buildFromAnnotation(method
							.getAnnotation(ApiResponseObject.class), method));
				}

				if(method.isAnnotationPresent(ApiErrors.class)) {
					apiMethodDoc.setApierrors(ApiErrorDoc.buildFromAnnotation(method.getAnnotation
							(ApiErrors.class)));
				}

				apiMethodDocs.add(apiMethodDoc);
			}

		}
		return apiMethodDocs;
	}

	public static String getObjectNameFromAnnotatedClass(Class<?> clazz) {

		Class<?> annotatedClass = ReflectionUtils.forName(clazz.getName());
		if(annotatedClass.isAnnotationPresent(ApiObject.class)) {
			return annotatedClass.getAnnotation(ApiObject.class).name();
		}
		return clazz.getSimpleName().toLowerCase();
	}

	public static boolean isMultiple(Method method) {

		if(Collection.class.isAssignableFrom(method.getReturnType()) || method.getReturnType()
		                                                                      .isArray()) {
			return true;
		}
		return false;
	}

	public static boolean isMultiple(Class<?> clazz) {

		if(Collection.class.isAssignableFrom(clazz) || clazz.isArray()) {
			return true;
		}
		return false;
	}

}
