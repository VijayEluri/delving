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

package de.offis.europeana.mobile;

/**
 * This class wraps the result of a device detection to an object that is used as return-value
 *
 * @author Dennis Heinen <Dennis.Heinen@offis.de>
 */
public class DeviceRecognitionResult {
    private final String _templateName;
    private final int _deviceScreenWidth;
    private final int _deviceScreenHeight;
    private final Boolean _coverflowEnabled;
    private final Boolean _isIEMobile;

    public DeviceRecognitionResult(String templateName, int deviceScreenWidth, int deviceScreenHeight, Boolean coverflowEnabled, Boolean isIEMobile) {
        _templateName = templateName;
        _deviceScreenWidth = deviceScreenWidth;
        _deviceScreenHeight = deviceScreenHeight;
        _coverflowEnabled = coverflowEnabled;
        _isIEMobile = isIEMobile;
    }

    /**
     * Returns a template name
     *
     * @return Best suited template for mobile device
     */
    public String GetTemplateName() {
        return _templateName;
    }

    public int GetDeviceScreenWidth() {
        return _deviceScreenWidth;
    }

    public int GetDeviceScreenHeight() {
        return _deviceScreenHeight;
    }

    public Boolean GetCoverflowEnabled(){
        return _coverflowEnabled;
    }

    public Boolean GetIsIEMobile(){
        return _isIEMobile;
    }
}