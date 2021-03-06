/*
 * Copyright (c) 2018 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sweers.catchup.ui.base

import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.autodispose.ControllerEvent
import com.bluelinelabs.conductor.autodispose.ControllerLifecycleSubjectHelper
import com.uber.autodispose.OutsideScopeException
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.KotlinLifecycleScopeProvider
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.Observable

class KotlinControllerScopeProvider private constructor(
    controller: Controller) : KotlinLifecycleScopeProvider<ControllerEvent> {

  companion object {
    fun from(controller: Controller): LifecycleScopeProvider<ControllerEvent> {
      return KotlinControllerScopeProvider(controller)
    }

    private val CORRESPONDING_EVENTS = CorrespondingEventsFunction { lastEvent: ControllerEvent ->
      when (lastEvent) {
        ControllerEvent.CREATE -> ControllerEvent.DESTROY
        ControllerEvent.CONTEXT_AVAILABLE -> ControllerEvent.CONTEXT_UNAVAILABLE
        ControllerEvent.CREATE_VIEW -> ControllerEvent.DESTROY_VIEW
        ControllerEvent.ATTACH -> ControllerEvent.DETACH
        ControllerEvent.DETACH -> ControllerEvent.DESTROY
        else -> throw OutsideScopeException(
            "Cannot bind to Controller lifecycle when outside of it.")
      }
    }
  }

  private val lifecycleSubject = ControllerLifecycleSubjectHelper.create(controller)

  override fun correspondingEvents() = CORRESPONDING_EVENTS

  override fun lifecycle(): Observable<ControllerEvent> = lifecycleSubject.hide()

  override fun peekLifecycle() = lifecycleSubject.value
}
