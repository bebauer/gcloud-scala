package gcloud.scala.pubsub

import com.google.api.core.{ApiFuture, ApiFutureCallback, ApiFutures}
import com.google.common.util.concurrent.MoreExecutors

import scala.concurrent.{Future, Promise}

private[pubsub] object FutureConversions {

  implicit class ApiFutureConversion[T](val future: ApiFuture[T]) extends AnyVal {
    def asScala: Future[T] = {
      val promise = Promise[T]()

      ApiFutures
        .addCallback(
          future,
          new ApiFutureCallback[T] {
            override def onFailure(t: Throwable): Unit = promise.failure(t)

            override def onSuccess(result: T): Unit = promise.success(result)
          },
          MoreExecutors.directExecutor()
        )

      promise.future
    }
  }

  object Implicits {
    import scala.language.implicitConversions

    implicit def apiFutureToScalaFuture[T](future: ApiFuture[T]): Future[T] = future.asScala
  }
}
