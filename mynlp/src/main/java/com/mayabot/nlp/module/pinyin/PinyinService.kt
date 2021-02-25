/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mayabot.nlp.module.pinyin

import com.mayabot.nlp.MynlpConfigs.pinyinExtDicSetting
import com.mayabot.nlp.MynlpConfigs.pinyinSetting
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.common.injector.Singleton
import com.mayabot.nlp.common.resources.NlpResource
import com.mayabot.nlp.module.pinyin.model.Pinyin
import java.util.*

/**
 * 拼音的词典
 *
 * @author jimichan
 */
@Singleton
class PinyinService constructor(private val env: MynlpEnv) : BasePinyinDictionary() {

    init {
        rebuild()
    }

    override fun load(): TreeMap<String, Array<Pinyin>> {
        val list = ArrayList<NlpResource?>()

        list.add(env.loadResource(env.get(pinyinSetting)))

        val ext = env.tryLoadResource(pinyinExtDicSetting)
        if (ext != null) {
            list.add(ext)
        }

        val map = TreeMap<String, Array<Pinyin>>()
        for (dictResource in list.filterNotNull()) {
            dictResource.inputStream()
                    .bufferedReader()
                    .forEachLine { line ->
                        //降龙伏虎=xiang2,long2,fu2,hu3
                        //单=dan1,shan4,chan2
                        val param = line.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        val key = param[0]
                        try {
                            val pinyins = parse(param[1])
                            if (pinyins != null) {
                                map[key] = pinyins
                            }
                        } catch (e: java.lang.Exception) {
                            logger.error("parse ${param[1]}", e)
                        }
                    }
        }

        return map
    }


}
