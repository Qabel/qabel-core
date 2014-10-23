package de.qabel.core.config;

import java.io.IOException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class AccountsTypeAdapter extends TypeAdapter<Accounts> {

	@Override
	public void write(JsonWriter out, Accounts value) throws IOException {
		out.beginArray();
		Gson gson = new Gson();
		Set<Account> set = value.getAccount();
		TypeAdapter<Account> adapter = gson.getAdapter(Account.class);
		for(Account account : set) {
			adapter.write(out, account);
		}
		out.endArray();
		return;
	}

	@Override
	public Accounts read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		Gson gson = new Gson();
		Accounts accounts = new Accounts();
		TypeAdapter<Account> adapter = gson.getAdapter(Account.class);
		Account account = null; 
		
		in.beginArray();
		while(in.hasNext()) {
			account = adapter.read(in);
			accounts.add(account);
		}
		in.endArray();
		
		return accounts;
	}
	
}
