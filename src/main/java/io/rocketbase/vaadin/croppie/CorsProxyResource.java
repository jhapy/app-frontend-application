/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rocketbase.vaadin.croppie;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import java.io.InputStream;
import java.net.URL;
import lombok.SneakyThrows;


public class CorsProxyResource extends StreamResource {

  public CorsProxyResource(String filename, String url) {
    super(filename,
        new InputStreamFactory() {
          @SneakyThrows
          @Override
          public InputStream createInputStream() {
            return new URL(url).openStream();
          }
        }

    );
  }
}
