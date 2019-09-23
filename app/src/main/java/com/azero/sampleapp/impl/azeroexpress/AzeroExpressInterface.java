/*
 * Copyright (c) 2019 SoundAI. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.azero.sampleapp.impl.azeroexpress;

import org.json.JSONObject;

/**
 * 从{@link com.azero.platforms.iface.AzeroExpress}中接收内容的模块需要实现此接口
 */
public interface AzeroExpressInterface {
    /**
     * 接收Directive
     *
     * @param payload Directive中的payload字段
     */
    void handleDirective(JSONObject payload);
}
