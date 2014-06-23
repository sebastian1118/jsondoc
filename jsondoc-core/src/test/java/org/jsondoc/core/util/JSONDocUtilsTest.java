package org.jsondoc.core.util;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsondoc.core.pojo.JSONDoc;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JSONDocUtilsTest {

	private String       version      = "1.0";

	private String       basePath     = "http://localhost:8080/api";

	private ObjectMapper objectMapper = new ObjectMapper();

	private static Logger log = LoggerFactory.getLogger(JSONDocUtilsTest.class);

	@Test
	public void getJSONDoc() throws JsonGenerationException, JsonMappingException, IOException {

		JSONDoc jsondoc
				= JSONDocUtils.getApiDoc(version, basePath, Lists.newArrayList("org.jsondoc.core" +
				".util"));
		Assert.assertEquals(2, jsondoc.getApis().size());
		Assert.assertEquals(2, jsondoc.getObjects().size());
		log.debug(objectMapper.writeValueAsString(jsondoc));
	}

}
