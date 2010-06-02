/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.xml;

/**
 * Remove the frightening things from tags so that they can become proper variable names.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Sanitizer {

    public static String tag2variable(String s) {
        s = s.replaceAll("[����]", "e");
        s = s.replaceAll("[��]", "u");
        s = s.replaceAll("[��]", "i");
        s = s.replaceAll("[��]", "a");
        s = s.replaceAll("�", "o");
        s = s.replaceAll("[���]", "E");
        s = s.replaceAll("[��]", "U");
        s = s.replaceAll("[��]", "I");
        s = s.replaceAll("[��]", "A");
        s = s.replaceAll("�", "O");
        s = s.replaceAll("-", "_");
        return s.toLowerCase();
    }
}
