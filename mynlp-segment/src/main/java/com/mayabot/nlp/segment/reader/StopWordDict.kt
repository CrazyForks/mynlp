package com.mayabot.nlp.segment.reader

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.google.inject.Singleton
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.collection.dat.DoubleArrayTrieMap
import java.util.*
import kotlin.collections.HashSet

const val StopWordDictPath = "stopwords.txt"

/**
 * 停用词接口
 *
 * Guice默认注入SystemStopWordDict
 *
 * @author jimichan
 */
@ImplementedBy(SystemStopWordDict::class)
interface StopWordDict {
    fun contains(word: String): Boolean
    fun add(word: String)
    fun remove(word: String)
    fun commit()
}


/**
 * 停用词词典,基于DAT的实现
 *
 * 可以动态新增、删减停用词。修改后需要[commit]操作。
 *
 * @author jimichan
 */
class DefaultStopWordDict(set: Set<String>) : StopWordDict {

    private var stopWordSet = HashSet(set)

    private var dat: DoubleArrayTrieMap<Boolean> = DoubleArrayTrieMap(
            TreeMap<String, Boolean>().apply { put("This Is Empty Flag", true) })
    private var isEmpty = false

    init {
        commit()
    }

    override fun commit() {
        if (stopWordSet.isEmpty()) {
            isEmpty = true
            return
        }
        isEmpty = false
        val treeMap = TreeMap<String, Boolean>()
        stopWordSet.forEach {
            treeMap[it] = true
        }
        dat = DoubleArrayTrieMap(treeMap)
    }

    override fun add(word: String) {
        stopWordSet.add(word)
    }

    override fun remove(word: String) {
        stopWordSet.remove(word)
    }


    override fun contains(word: String) = dat.containsKey(word)

}

/**
 * 停用词词典,从系统中加载停用词词典
 *
 * @author jimichan
 */
@Singleton
class SystemStopWordDict
@Inject constructor(val env: MynlpEnv) : StopWordDict {

    private val stopDict = DefaultStopWordDict(loadStopword())

    override fun contains(word: String): Boolean {
        return stopDict.contains(word)
    }

    override fun commit() {
        stopDict.commit()
    }

    override fun add(word: String) {
        stopDict.add(word)
    }

    override fun remove(word: String) {
        stopDict.remove(word)
    }

    private fun loadStopword(): Set<String> {

        try {
            val resource = env.tryLoadResource(StopWordDictPath)

            resource?.let { re ->
                return re.inputStream().bufferedReader().readLines().asSequence()
                        .map { it.trim() }.filter { it.isNotBlank() }.toSet()

            }
        } catch (e: Exception) {
            Mynlps.logger.error("", e)
        }

        return emptySet()
    }
}
