package de.qabel.core.config;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

public class ModuleSettingsTest {
	@Test
	public void serializeModuleSettings() throws IOException {
		SyncedModuleSettings fooSettings = new FooModuleSettings(42);
		TypeAdapter<SyncedModuleSettings> adapter = new GsonBuilder()
			.registerTypeAdapter(SyncedModuleSettings.class, new SyncedModuleSettingsTypeAdapter())
			.create().getAdapter(SyncedModuleSettings.class);
		String json = adapter.toJson(fooSettings);
		System.out.println(json);
		Assert.assertEquals(fooSettings, adapter.fromJson(json));
	}
}

class FooModuleSettings extends SyncedModuleSettings {
	int answer;
	
	public FooModuleSettings(int answer) {
		this.answer = answer;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FooModuleSettings other = (FooModuleSettings) obj;
		if (answer != other.answer) {
			return false;
		}
		return true;
	}
}