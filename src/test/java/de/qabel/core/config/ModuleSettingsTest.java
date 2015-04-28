package de.qabel.core.config;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

public class ModuleSettingsTest {

	private static class FooModuleSettings extends AbstractModuleSettings {
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
	
	@Test
	public void serializeModuleSettings() throws IOException {
		FooModuleSettings fooSettings = new FooModuleSettings(42);
		TypeAdapter<? super FooModuleSettings> adapter = new GsonBuilder()
			.registerTypeAdapter(AbstractModuleSettings.class, new AbstractModuleSettingsTypeAdapter())
			.create().getAdapter(FooModuleSettings.class);
		String json = adapter.toJson(fooSettings);
		Assert.assertEquals(fooSettings, adapter.fromJson(json));
	}
}
