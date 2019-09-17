package com.mayabot.nlp.segment

import com.google.inject.Key
import com.google.inject.name.Names
import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.segment.lexer.core.CoreDictionary
import com.mayabot.nlp.segment.lexer.core.CoreDictionaryImpl
import com.mayabot.nlp.segment.lexer.core.CoreLexerPlugin
import com.mayabot.nlp.segment.lexer.perceptron.CwsLexerPlugin
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin
import com.mayabot.nlp.segment.plugins.collector.DictBasedFillSubword
import com.mayabot.nlp.segment.plugins.collector.IndexPickUpSubword
import com.mayabot.nlp.segment.plugins.collector.SentenceCollector
import com.mayabot.nlp.segment.plugins.collector.WordTermCollector
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionary
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin
import com.mayabot.nlp.segment.plugins.ner.NerPlugin
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin
import com.mayabot.nlp.segment.plugins.pos.PosPlugin

/**
 * Fluent style
 * @author jimichan
 */
open class FluentLexerBuilder : LexerBuilder {

    companion object {
        @JvmStatic
        fun builder() = FluentLexerBuilder()
    }

    override fun build(): Lexer {
        return builder.build()
    }

    val builder = PipelineLexerBuilder.builder()

    fun basic() = BasicBlock()

    fun withPos(): FluentLexerBuilder {
        builder.install(PosPlugin())
        return this
    }

    fun withPersonName(): FluentLexerBuilder {
        builder.install(PersonNamePlugin())
        return this
    }

    fun withNer(): FluentLexerBuilder {
        builder.install(NerPlugin())
        return this
    }

    fun withCustomDictionary(dict: CustomDictionary): FluentLexerBuilder {
        builder.install(CustomDictionaryPlugin(dict))
        return this
    }

    fun withCustomDictionary(): FluentLexerBuilder {
        builder.install(CustomDictionaryPlugin())
        return this
    }

    fun with(plugin: PipelineLexerPlugin) : FluentLexerBuilder {
        builder.install(plugin)
        return this
    }

    /**
     * 保持字符原样输出
     */
    fun keepOriCharOutput() : FluentLexerBuilder {
        builder.setKeepOriCharOutput(true)
        return this
    }

    fun collector(): CollectorBlock {
        return CollectorBlock()
    }

    inner class CollectorBlock {

        val collector: WordTermCollector = SentenceCollector()

        fun pickUpSubword(pickUpSubword: WordTermCollector.PickUpSubword): CollectorBlock {
            collector.pickUpSubword = pickUpSubword
            return this
        }

        fun fillSubword(fillSubword: WordTermCollector.FillSubword): CollectorBlock {
            collector.fillSubword = fillSubword
            return this
        }

        @JvmOverloads
        fun indexPickup(minWordLen: Int = 2): CollectorBlock {
            val indexd = IndexPickUpSubword()
//
            indexd.minWordLength = minWordLen
            collector.pickUpSubword = indexd
            return this
        }

        @JvmOverloads
        fun smartPickup(block: (x: WordTermCollector.PickUpSubword) -> Unit
                        = { _ -> Unit }
        ): CollectorBlock {
            try {
                // 松散绑定name = smart的Bean，在企业版中提供
                val p = Mynlps.get()
                        .injector
                        .getInstance(
                                Key.get(WordTermCollector.PickUpSubword::class.java
                                        , Names.named("smart")))!!
                block(p)
                collector.pickUpSubword = p
            } catch (e: Exception) {
                Mynlps.logger.warn("企业版才可以调用这个方法", e)
                throw e
            }

            return this
        }

        @JvmOverloads
        fun fillSubwordDict(dbcms: CoreDictionary = Mynlps.instanceOf(CoreDictionaryImpl::class.java)): CollectorBlock {
            collector.fillSubword = DictBasedFillSubword(dbcms)
            return this
        }

        fun with(collector: WordTermCollector): CollectorBlock {
            builder.termCollector = collector
            return this
        }

        fun done(): FluentLexerBuilder {
            builder.termCollector = collector
            return this@FluentLexerBuilder
        }
    }

    inner class BasicBlock {
        fun core(): FluentLexerBuilder {
            builder.install(CoreLexerPlugin())
            return this@FluentLexerBuilder
        }

        fun coreByDict(dict: CoreDictionary): FluentLexerBuilder {
            builder.install(CoreLexerPlugin(dict))
            return this@FluentLexerBuilder
        }

        fun cws(): FluentLexerBuilder {
            builder.install(CwsLexerPlugin())
            return this@FluentLexerBuilder
        }
    }


}


