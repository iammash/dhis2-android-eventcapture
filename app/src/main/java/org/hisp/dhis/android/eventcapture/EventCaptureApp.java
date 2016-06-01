/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.hisp.dhis.client.sdk.android.api.D2;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public final class EventCaptureApp extends Application {

    @Inject
    D2.Flavor flavor;

    AppComponent appComponent;

    UserComponent userComponent;

    // we need to preserve this component unless someone explicitly asks to remove it
    FormComponent formComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // enabling crashlytics only for release builds
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build();
        Fabric.with(this, crashlytics);

        // Global dependency graph
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        // injecting dependencies
        appComponent.inject(this);

        // initializing stetho
        // Stetho.initializeWithDefaults(this);
        D2.init(this, flavor);

        // adding UserComponent to global dependency graph
        userComponent = appComponent.plus(new UserModule());

        // TODO Add LeakCanary support
        // TODO implement debug navigation drawer
    }

    @Override
    protected void attachBaseContext(Context baseContext) {
        super.attachBaseContext(baseContext);

        // TODO we should reduce amount of methods
        MultiDex.install(this);
    }

    public UserComponent createUserComponent(String serverUrl) {
        userComponent = appComponent.plus(new UserModule(serverUrl));
        return userComponent;
    }

    public FormComponent createFormComponent() {
        isNull(userComponent, "UserComponent must not be null");
        formComponent = userComponent.plus(new FormModule());
        return formComponent;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public UserComponent getUserComponent() {
        return userComponent;
    }

    public FormComponent getFormComponent() {
        return formComponent;
    }

    public void releaseFormComponent() {
        formComponent = null;
    }
}
