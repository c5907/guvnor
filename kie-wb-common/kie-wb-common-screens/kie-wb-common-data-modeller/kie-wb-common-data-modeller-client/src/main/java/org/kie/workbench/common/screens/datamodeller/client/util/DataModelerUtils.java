/**
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.client.util;

import org.kie.workbench.common.screens.datamodeller.model.DataObjectTO;

public class DataModelerUtils {

    public static final String EXTERNAL_PREFIX = "- ext - ";
    public static final String MULTIPLE = " [0..N]";

    public static DataModelerUtils getInstance() {
        return new DataModelerUtils();
    }

    public static String getDataObjectUILabel(DataObjectTO dataObject) {
        if (dataObject != null) {
            String label = dataObject.getLabel();
            if (label != null) return label;
            else return dataObject.getName();
        }
        return "";
    }

    public static String getDataObjectFullLabel(DataObjectTO dataObject) {
        StringBuilder sb = new StringBuilder();
        sb.append(dataObject.getClassName());
        String objectLabel = dataObject.getLabel();
        if (objectLabel != null) sb.insert(0, objectLabel + " (").append(")");
        return sb.toString();
    }

    public String extractClassName(String fullClassName) {

        if (fullClassName == null) return null;
        int index = fullClassName.lastIndexOf(".");
        if (index > 0) {
            return fullClassName.substring(index+1, fullClassName.length());

        } else {
            return fullClassName;
        }
    }

    public String extractPackageName(String fullClassName) {
        if (fullClassName == null) return null;
        int index = fullClassName.lastIndexOf(".");
        if (index > 0) {
            return fullClassName.substring(0, index);

        } else {
            return null;
        }
    }

    public String[] getPackageTerms(String packageName) {
        return packageName.split("\\.", -1);
    }

    public String unCapitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen)
                .append(Character.toLowerCase(str.charAt(0)))
                .append(str.substring(1))
                .toString();
    }
}