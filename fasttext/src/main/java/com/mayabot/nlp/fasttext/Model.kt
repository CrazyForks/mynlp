package com.mayabot.nlp.fasttext

import com.carrotsearch.hppc.IntArrayList
import com.mayabot.nlp.fasttext.blas.FloatMatrix
import com.mayabot.nlp.fasttext.blas.vector.FloatArrayVector
import com.mayabot.nlp.fasttext.loss.Loss
import kotlin.random.Random

typealias Predictions = MutableList<ScoreIdPair>

class Model(
        val wi: FloatMatrix,
        val wo: FloatMatrix,
        val loss: Loss,
        val normalizeGradient: Boolean
) {

    companion object {
        val kUnlimitedPredictions: Int = -1
        val kAllLabelsAsTarget = -1
    }

    /**
     * input里面存放的是row Id,这些来自input matrix
     */
    private fun computeHidden(input: IntArrayList, state: State) {
        val hidden = state.hidden
        hidden.zero()

        input.forEach2 { row ->
            hidden += wi[row]
        }

        //长度归一化
        hidden *= (1.0f / input.size())
    }

    /**
     * 预测分类结果
     *
     * 预测过程。。。
     *
     * @param input 输入的词的下标
     *
     */
    fun predict(input: IntArrayList,
                k: Int,
                threshold: Float,
                heap: Predictions,
                state: State
    ) {
        val kk = if (k == kUnlimitedPredictions) {
            // output size
            wo.rows()
        } else{
            k
        }
        if (kk == 0) {
            throw RuntimeException("k needs to be 1 or higher")
        }

        computeHidden(input,state)

        loss.predict(k,threshold,heap,state)
    }

    fun update(input: IntArrayList,
               targets:IntArrayList,
               targetIndex:Int,
               lr:Float,
               state:State){
        if (input.size() == 0) {
            return
        }

        computeHidden(input,state)

        val grad = state.grad
        grad.zero()

        val lossValue = loss.forward(targets,targetIndex,state,lr,true)

        state.incrementNExamples(lossValue)

        if (normalizeGradient) {
            grad *= (1.0f/input.size())
        }

        input.forEach2 { i->
            wi.addVectorToRow(grad,i,1.0f)
        }
    }

    class State(hiddenSize: Int, outputSize: Int, seed: Int) {
        private var lossValue = 0.0f
        private var nexamples = 0

        val hidden = FloatArrayVector(hiddenSize)
        val output = FloatArrayVector(outputSize)
        val grad = FloatArrayVector(hiddenSize)
        val rng = Random(seed)

        val loss get() = lossValue / nexamples

        fun incrementNExamples(loss: Float) {
            lossValue += loss
            nexamples++
        }
    }

}