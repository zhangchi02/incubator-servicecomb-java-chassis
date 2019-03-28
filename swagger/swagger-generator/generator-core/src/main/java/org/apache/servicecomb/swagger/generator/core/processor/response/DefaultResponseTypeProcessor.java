/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.swagger.generator.core.processor.response;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findResponseTypeProcessor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ResponseTypeProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.converter.ModelConverters;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.utils.PropertyModelConverter;
import io.swagger.util.ReflectionUtils;

public class DefaultResponseTypeProcessor implements ResponseTypeProcessor {
  @Override
  public Type getProcessType() {
    // not care for this.
    return null;
  }

  @Override
  public Model process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      Type genericResponseType) {
    if (!(genericResponseType instanceof ParameterizedType)) {
      return doProcess(swaggerGenerator, operationGenerator, genericResponseType);
    }

    // eg:
    //   genericResponseType is CompletableFuture<ResponseEntity<String>>
    //   responseType is ResponseEntity<String>
    //   responseRawType is ResponseEntity
    Type responseType = ((ParameterizedType) genericResponseType).getActualTypeArguments()[0];
    Type responseRawType = responseType;
    if (responseType instanceof ParameterizedType) {
      responseRawType = ((ParameterizedType) responseType).getRawType();
    }

    ResponseTypeProcessor processor = findResponseTypeProcessor(responseRawType);
    if (responseRawType.equals(processor.getProcessType())) {
      return processor.process(swaggerGenerator, operationGenerator, responseType);
    }

    return doProcess(swaggerGenerator, operationGenerator, genericResponseType);
  }

  protected Model doProcess(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      Type responseType) {
    if (ReflectionUtils.isVoid(responseType)) {
      return null;
    }
    SwaggerUtils.addDefinitions(swaggerGenerator.getSwagger(), responseType);
    Property property = ModelConverters.getInstance().readAsProperty(responseType);
    return new PropertyModelConverter().propertyToModel(property);
  }
}
