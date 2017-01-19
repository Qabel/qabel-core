package de.qabel.client.box

import rx.Scheduler

interface BoxSchedulers {

    val io : Scheduler

}

class MainBoxSchedulers(override val io: Scheduler) : BoxSchedulers
