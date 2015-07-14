package de.qabel.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.EventNameConstants;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import de.qabel.ackack.Actor;
import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.Responsible;

/**
 * Actor which handles the access to the configuration of Qabel.
 *
 */
public class ResourceActor extends Actor {
	private final Contacts contacts;
	private final Settings settings;
	private EventEmitter eventEmitter;
	private Persistence persistence;

	private static final String RETRIEVE_CONTACTS = "retrieveContacts";
	private static final String RETRIEVE_ACCOUNTS = "retrieveAccounts";
	private static final String RETRIEVE_DROPSERVERS = "retrieveDropServers";
	private static final String RETRIEVE_IDENTITIES = "retrieveIdentities";
	private static final String RETRIEVE_LOCALMODULESETTINGS = "retrieveLocalModuleSettings";
	private static final String RETRIEVE_LOCALSETTINGS = "retrieveLocalSettings";
	private static final String RETRIEVE_STORAGESERVERS = "retrieveStorageServers";
	private static final String RETRIEVE_STORAGEVOLUMES = "retrieveStorageVolumes";
	private static final String RETRIEVE_SYNCEDMODULESETTINGS = "retrieveSyncedModuleSettings";

	private static final String WRITE_CONTACTS = "writeContacts";
	private static final String WRITE_ACCOUNTS = "writeAccounts";
	private static final String WRITE_DROPSERVERS = "writeDropServers";
	private static final String WRITE_IDENTITIES = "writeIdentities";
	private static final String WRITE_LOCALMODULESETTINGS = "writeLocalModuleSettings";
	private static final String WRITE_LOCALSETTINGS = "writeLocalSettings";
	private static final String WRITE_STORAGESERVERS = "writeStorageServers";
	private static final String WRITE_STORAGEVOLUMES = "writeStorageVolumes";
	private static final String WRITE_SYNCEDMODULESETTINGS = "writeSyncedModuleSettings";

	private static final String REMOVE_CONTACTS = "removeContacts";
	private static final String REMOVE_ACCOUNTS = "removeAccounts";
	private static final String REMOVE_DROPSERVERS = "removeDropServers";
	private static final String REMOVE_IDENTITIES = "removeIdentities";
	private static final String REMOVE_LOCALMODULESETTINGS = "removeLocalModuleSettings";
	private static final String REMOVE_STORAGESERVERS = "removeStorageServers";
	private static final String REMOVE_STORAGEVOLUMES = "removeStorageVolumes";
	private static final String REMOVE_SYNCEDMODULESETTINGS = "removeSyncedModuleSettings";

	private final static Logger logger = LoggerFactory.getLogger(ResourceActor.class.getName());

	public ResourceActor(Persistence<String> persistence, EventEmitter eventEmitter) {
		this.persistence = persistence;
		this.settings = new Settings();
		this.contacts = new Contacts();
		//TODO: DEFAULT SETTINGS?!?
		settings.setLocalSettings(new LocalSettings(1000L, new Date()));
		settings.setSyncedSettings(new SyncedSettings());
		this.eventEmitter = eventEmitter;
		loadFromPersistence();
	}

	private void loadFromPersistence() {
		for (Object object: persistence.getEntities(Contact.class)) {
			contacts.put((Contact) object);
		}
		for (Object object: persistence.getEntities(Account.class)) {
			settings.getSyncedSettings().getAccounts().put((Account) object);
		}

		for (Object object: persistence.getEntities(DropServer.class)) {
			settings.getSyncedSettings().getDropServers().put((DropServer) object);
		}

		for (Object object: persistence.getEntities(Identity.class)) {
			settings.getSyncedSettings().getIdentities().put((Identity) object);
		}

		for (Object object: persistence.getEntities(LocaleModuleSettings.class)) {
			settings.getLocalSettings().getLocaleModuleSettings().add((LocaleModuleSettings) object);
		}

		for (Object object: persistence.getEntities(LocalSettings.class)) {
			settings.setLocalSettings((LocalSettings) object);
		}

		for (Object object: persistence.getEntities(StorageServer.class)) {
			settings.getSyncedSettings().getStorageServers().put((StorageServer) object);
		}

		for (Object object: persistence.getEntities(StorageVolume.class)) {
			settings.getSyncedSettings().getStorageVolumes().put((StorageVolume) object);
		}

		for (Object object: persistence.getEntities(SyncedModuleSettings.class)) {
			settings.getSyncedSettings().getSyncedModuleSettings().add((SyncedModuleSettings) object);
		}
	}

	/**
	 * Add new and write changed contacts
	 * @param contacts Contacts to be written
	 * @return True if contacts have been sent to actor
	 */
	public boolean writeContacts(final Contact... contacts) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_CONTACTS);
		return post(info, (Serializable[]) contacts);
	}

	/**
	 * Retrieve contacts by key identifier. If no key identifier is passed all
	 * contacts will be retrieved.
	 * @param sender Sending actor
	 * @param responsible Class to handle the call back
	 * @param keyIdentifiers Key identifiers of requested contacts (all if empty)
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveContacts(Actor sender, Responsible responsible, final String... keyIdentifiers) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_CONTACTS);
		return post(info, (Serializable[]) keyIdentifiers);
	}

	/**
	 * Remove one or more contacts.
	 * @param keyIdentifiers Key identifiers of contacts to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeContacts(final String... keyIdentifiers) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_CONTACTS);
		return post(info, (Serializable[]) keyIdentifiers);
	}

	/**
	 * Retrieve all accounts
	 * @param sender Sending actor
	 * @param responsible Class to handle the call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveAccounts(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_ACCOUNTS);
		return post(info);
	}

	/**
	 * Add new and write changed accounts.
	 * @param accounts Accounts to be written
	 * @return True if accounts have been sent to actor
	 */
	public boolean writeAccounts(final Account...accounts) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_ACCOUNTS);
		return post(info, (Serializable[]) accounts);
	}

	/**
	 * Remove accounts.
	 * @param accounts Accounts to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeAccounts(final Account...accounts) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_ACCOUNTS);
		return post(info, (Serializable[]) accounts);
	}

	/**
	 * Retrieve all drop servers.
	 * @param sender Sending actor
	 * @param responsible Class to handle the call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveDropServers(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_DROPSERVERS);
		return post(info);
	}

	/**
	 * Add new and write changed drop servers.
	 * @param dropServers Drop servers to be written
	 * @return True if drop servers have been sent to actor
	 */
	public boolean writeDropServers(final DropServer...dropServers) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_DROPSERVERS);
		return post(info, (Serializable[]) dropServers);
	}

	/**
	 * Remove drop servers.
	 * @param dropServers Drop servers to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeDropServers(final DropServer...dropServers) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_DROPSERVERS);
		return post(info, (Serializable[]) dropServers);
	}

	/**
	 * Retrieve all identities.
	 * @param sender Sending actor
	 * @param responsible Class to handle the call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveIdentities(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_IDENTITIES);
		return post(info);
	}

	/**
	 * Add new and write changed identities.
	 * @param identities Identities to be written
	 * @return True if identities have been sent to actor
	 */
	public boolean writeIdentities(final Identity...identities) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_IDENTITIES);
		return post(info, (Serializable[]) identities);
	}

	/**
	 * Remove identities.
	 * @param identities Identities to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeIdentities(final Identity...identities) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_IDENTITIES);
		return post(info, (Serializable[]) identities);
	}

	/**
	 * Retrieve all local module settings.
	 * @param sender Sending actor
	 * @param responsible Class to handle the call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveLocalModuleSettings(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_LOCALMODULESETTINGS);
		return post(info);
	}

	/**
	 * Add new and write changed local module settings
	 * @param localModuleSettings Local module settings to be written
	 * @return True if local module settings have been sent to actor
	 */
	public boolean writeLocalModuleSettings(final LocaleModuleSettings...localModuleSettings) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_LOCALMODULESETTINGS);
		return post(info, (Serializable[]) localModuleSettings);
	}

	/**
	 * Remove local module settings.
	 * @param localModuleSettings Local module settings to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeLocalModuleSettings(final LocaleModuleSettings...localModuleSettings) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_LOCALMODULESETTINGS);
		return post(info, (Serializable[]) localModuleSettings);
	}

	/**
	 * Retrieve local settings.
	 * @param sender Sending actor
	 * @param responsible Class to handle call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveLocalSettings(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_LOCALSETTINGS);
		return post(info);
	}

	/**
	 * Add new and write changed local settings
	 * @param localSettings Local settings to be written
	 * @return True if local settings have been sent to actor
	 */
	public boolean writeLocalSettings(final LocalSettings...localSettings) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_LOCALSETTINGS);
		return post(info, (Serializable[]) localSettings);
	}

	/**
	 * Retrieve all storage servers.
	 * @param sender Sending actor
	 * @param responsible Class to handle call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveStorageServers(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_STORAGESERVERS);
		return post(info);
	}

	/**
	 * Add new and write changed storage servers.
	 * @param storageServers Storage servers to be written
	 * @return True if storage servers have been sent to actor
	 */
	public boolean writeStorageServers(final StorageServer...storageServers) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_STORAGESERVERS);
		return post(info, (Serializable[]) storageServers);
	}

	/**
	 * Remove storage servers.
	 * @param storageServers Storage servers to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeStorageServers(final StorageServer...storageServers) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_STORAGESERVERS);
		return post(info, (Serializable[]) storageServers);
	}

	/**
	 * Retrieve all storage volumes
	 * @param sender Sending actor
	 * @param responsible Class to handle call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveStorageVolumes(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_STORAGEVOLUMES);
		return post(info);
	}

	/**
	 * Add new and write changed storage volumes
	 * @param storageVolumes Storage volumes to be written
	 * @return True if storage volumes have been sent to actor
	 */
	public boolean writeStorageVolumes(final StorageVolume...storageVolumes) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_STORAGEVOLUMES);
		return post(info, (Serializable[]) storageVolumes);
	}

	/**
	 * Remove storage volumes.
	 * @param storageVolumes Storage volumes to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeStorageVolumes(final StorageVolume...storageVolumes) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_STORAGEVOLUMES);
		return post(info, (Serializable[]) storageVolumes);
	}

	/**
	 * Retrieve all synced module settings
	 * @param sender Sending actor
	 * @param responsible Class to handle call back
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveSyncedModuleSettings(Actor sender, Responsible responsible) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(RETRIEVE_SYNCEDMODULESETTINGS);
		return post(info);
	}

	/**
	 * Add new and write changed synced module settings
	 * @param syncedModuleSettings Synced module settings to be written
	 * @return True if synced module settings have been sent to actor
	 */
	public boolean writeSyncedModuleSettings(final SyncedModuleSettings...syncedModuleSettings) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_SYNCEDMODULESETTINGS);
		return post(info, (Serializable[]) syncedModuleSettings);
	}

	/**
	 * Remove synced module settings
	 * @param syncedModuleSettings Synced module settings to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeSyncedModuleSettings(final SyncedModuleSettings...syncedModuleSettings) {
		MessageInfo info = new MessageInfo();
		info.setType(REMOVE_SYNCEDMODULESETTINGS);
		return post(info, (Serializable[]) syncedModuleSettings);
	}

	@Override
	protected void react(MessageInfo info, Object... data) {
		switch (info.getType()) {
			case RETRIEVE_CONTACTS:
				Contact[] contactsArray;
				synchronized (contacts) {
					if(data.length > 0) {
						ArrayList<Contact> contactsList = new ArrayList<>();
						for (Object object : data) {
							contactsList.add(this.contacts.getByKeyIdentifier((String) object));
						}
						contactsArray = contactsList.toArray(new Contact[0]);
					} else {
						contactsArray = this.contacts.getContacts().toArray(new Contact[0]);
					}
					info.response((Serializable[]) contactsArray);
				}
				break;
			case RETRIEVE_ACCOUNTS:
				synchronized (settings.getSyncedSettings().getAccounts()) {
					info.response((Serializable[]) this.settings
							.getSyncedSettings().getAccounts().getAccounts()
							.toArray(new Account[0]));
				}
				break;
			case RETRIEVE_DROPSERVERS:
				synchronized (settings.getSyncedSettings().getDropServers()) {
					info.response((Serializable[]) this.settings
							.getSyncedSettings().getDropServers().getDropServers()
							.toArray(new DropServer[0]));
				}
				break;
			case RETRIEVE_IDENTITIES:
				synchronized (settings.getSyncedSettings().getIdentities()) {
					info.response((Serializable[]) this.settings
							.getSyncedSettings().getIdentities().getIdentities()
							.toArray(new Identity[0]));
				}
				break;
			case RETRIEVE_LOCALMODULESETTINGS:
				synchronized (settings.getLocalSettings().getLocaleModuleSettings()) {
					info.response((Serializable[]) this.settings
							.getLocalSettings().getLocaleModuleSettings()
							.toArray(new LocaleModuleSettings[0]));
				}
				break;
			case RETRIEVE_LOCALSETTINGS:
				synchronized (settings.getLocalSettings()) {
					info.response((Serializable) this.settings.getLocalSettings());
				}
				break;
			case RETRIEVE_STORAGESERVERS:
				synchronized (settings.getSyncedSettings().getStorageServers()) {
					info.response((Serializable[]) this.settings
							.getSyncedSettings().getStorageServers()
							.getStorageServers().toArray(new StorageServer[0]));
				}
				break;
			case RETRIEVE_STORAGEVOLUMES:
				synchronized (settings.getSyncedSettings().getStorageVolumes()) {
					info.response((Serializable[]) this.settings
							.getSyncedSettings().getStorageVolumes()
							.getStorageVolumes().toArray(new StorageVolume[0]));
				}
				break;
			case RETRIEVE_SYNCEDMODULESETTINGS:
				synchronized (settings.getSyncedSettings().getSyncedModuleSettings()) {
					info.response((Serializable[]) this.settings
							.getSyncedSettings().getSyncedModuleSettings()
							.toArray(new SyncedModuleSettings[0]));
				}
				break;
			case WRITE_CONTACTS:
				synchronized (contacts) {
					for (Object object : data) {
						Contact contact = (Contact) object;
						this.contacts.put(contact);
						persistence.updateOrPersistEntity(contact);
						eventEmitter.emit(EventNameConstants.EVENT_CONTACT_ADDED, contact);
					}
				}
				break;
			case WRITE_ACCOUNTS:
				synchronized (settings.getSyncedSettings().getAccounts()) {
					Accounts accounts = this.settings.getSyncedSettings().getAccounts();
					for (Object object : data) {
						Account account = (Account) object;
						accounts.put(account);
						persistence.updateOrPersistEntity(account);
						eventEmitter.emit(EventNameConstants.EVENT_ACCOUNT_ADDED, account);
					}
				}
				break;
			case WRITE_DROPSERVERS:
				synchronized (settings.getSyncedSettings().getDropServers()) {
					DropServers dropServers = this.settings.getSyncedSettings().getDropServers();
					for (Object object : data) {
						DropServer dropServer = (DropServer) object;
						dropServers.put(dropServer);
						persistence.updateOrPersistEntity(dropServer);
						eventEmitter.emit(EventNameConstants.EVENT_DROPSERVER_ADDED, dropServer);
					}
				}
				break;
			case WRITE_IDENTITIES:
				synchronized (settings.getSyncedSettings().getIdentities()) {
					Identities identities = this.settings.getSyncedSettings().getIdentities();
					for (Object object : data) {
						Identity identity = (Identity) object;
						identities.put(identity);
						persistence.updateOrPersistEntity(identity);
						eventEmitter.emit(EventNameConstants.EVENT_IDENTITY_ADDED, identity);
					}
				}
				break;
			case WRITE_LOCALMODULESETTINGS:
				synchronized (settings.getLocalSettings().getLocaleModuleSettings()) {
					Set<LocaleModuleSettings> localModuleSettingsList = this.settings.getLocalSettings()
							.getLocaleModuleSettings();
					for (Object object : data) {
						LocaleModuleSettings localModuleSettings = (LocaleModuleSettings) object;
						localModuleSettingsList.remove(localModuleSettings);
						localModuleSettingsList.add(localModuleSettings);
						persistence.updateOrPersistEntity(localModuleSettings);
						//TODO: EMIT THIS EVENT?
					}
				}
				break;
			case WRITE_LOCALSETTINGS:
				synchronized (settings.getLocalSettings()) {
					this.settings.setLocalSettings((LocalSettings) data[0]);
					persistence.updateOrPersistEntity((LocalSettings) data[0]);
				}
				break;
			case WRITE_STORAGESERVERS:
				synchronized (settings.getSyncedSettings().getStorageServers()) {
					StorageServers storageServers = this.settings.getSyncedSettings().getStorageServers();
					for (Object object : data) {
						StorageServer storageServer = (StorageServer) object;
						storageServers.put(storageServer);
						persistence.updateOrPersistEntity(storageServer);
						eventEmitter.emit(EventNameConstants.EVENT_STORAGESERVER_ADDED, storageServer);
					}
				}
				break;
			case WRITE_STORAGEVOLUMES:
				synchronized (settings.getSyncedSettings().getStorageVolumes()) {
					StorageVolumes storageVolumes = this.settings.getSyncedSettings().getStorageVolumes();
					for (Object object : data) {
						StorageVolume storageVolume = (StorageVolume) object;
						storageVolumes.put(storageVolume);
						persistence.updateOrPersistEntity(storageVolume);
						eventEmitter.emit(EventNameConstants.EVENT_STORAGEVOLUME_ADDED, storageVolume);
					}
				}
				break;
			case WRITE_SYNCEDMODULESETTINGS:
				synchronized (settings.getSyncedSettings().getSyncedModuleSettings()) {
					Set<SyncedModuleSettings> syncedModuleSettingsList = this.settings.getSyncedSettings()
							.getSyncedModuleSettings();
					for (Object object : data) {
						SyncedModuleSettings syncedModuleSettings = (SyncedModuleSettings) object;
						syncedModuleSettingsList.remove(syncedModuleSettings);
						syncedModuleSettingsList.add(syncedModuleSettings);
						persistence.updateOrPersistEntity(syncedModuleSettings);
						//TODO: EMIT THIS EVENT?
					}
				}
				break;
			case REMOVE_CONTACTS:
				synchronized (contacts) {
					for (Object object : data) {
						Contact c = contacts.getByKeyIdentifier(object.toString());
						if(c != null) {
							persistence.removeEntity(c.getPersistenceID(), Contact.class);
						}
						this.contacts.remove(object.toString());
						eventEmitter.emit(EventNameConstants.EVENT_CONTACT_REMOVED, object.toString());
					}
				}
				break;
			case REMOVE_ACCOUNTS:
				synchronized (settings.getSyncedSettings().getAccounts()) {
					Accounts accounts = this.settings.getSyncedSettings().getAccounts();
					for (Object object : data) {
						Account account = (Account) object;
						accounts.remove(account);
						persistence.removeEntity(account.getPersistenceID(), account.getClass());
						eventEmitter.emit(EventNameConstants.EVENT_ACCOUNT_REMOVED, account);
					}
				}
				break;
			case REMOVE_DROPSERVERS:
				synchronized (settings.getSyncedSettings().getDropServers()) {
					DropServers dropServers = this.settings.getSyncedSettings().getDropServers();
					for (Object object : data) {
						DropServer dropServer = (DropServer) object;
						dropServers.remove(dropServer);
						persistence.removeEntity(dropServer.getPersistenceID(), dropServer.getClass());
						eventEmitter.emit(EventNameConstants.EVENT_DROPSERVER_REMOVED, dropServer);
					}
				}
				break;
			case REMOVE_IDENTITIES:
				synchronized (settings.getSyncedSettings().getIdentities()) {
					Identities identities = this.settings.getSyncedSettings().getIdentities();
					for (Object object : data) {
						Identity identity = (Identity) object;
						identities.remove(identity);
						persistence.removeEntity(identity.getPersistenceID(), identity.getClass());
						eventEmitter.emit(EventNameConstants.EVENT_IDENTITY_REMOVED, identity.getKeyIdentifier());
					}
				}
				break;
			case REMOVE_LOCALMODULESETTINGS:
				synchronized (settings.getLocalSettings().getLocaleModuleSettings()) {
					Set<LocaleModuleSettings> localModuleSettingsList = this.settings.getLocalSettings()
							.getLocaleModuleSettings();
					for (Object object : data) {
						LocaleModuleSettings localModuleSettings = (LocaleModuleSettings) object;
						localModuleSettingsList.remove(localModuleSettings);
						persistence.removeEntity(localModuleSettings.getPersistenceID(), localModuleSettings.getClass());
						//TODO: EMIT THIS EVENT?
					}
				}
				break;
			case REMOVE_STORAGESERVERS:
				synchronized (settings.getSyncedSettings().getStorageServers()) {
					StorageServers storageServers = this.settings.getSyncedSettings().getStorageServers();
					for (Object object : data) {
						StorageServer storageServer = (StorageServer) object;
						storageServers.remove(storageServer);
						persistence.removeEntity(storageServer.getPersistenceID(), storageServer.getClass());
						eventEmitter.emit(EventNameConstants.EVENT_STORAGESERVER_REMOVED, storageServer);
					}
				}
				break;
			case REMOVE_STORAGEVOLUMES:
				synchronized (settings.getSyncedSettings().getStorageVolumes()) {
					StorageVolumes storageVolumes = this.settings.getSyncedSettings().getStorageVolumes();
					for (Object object : data) {
						StorageVolume storageVolume = (StorageVolume) object;
						storageVolumes.remove(storageVolume);
						persistence.removeEntity(storageVolume.getPersistenceID(), storageVolume.getClass());
						eventEmitter.emit(EventNameConstants.EVENT_STORAGEVOLUME_REMOVED, storageVolume);
					}
				}
				break;
			case REMOVE_SYNCEDMODULESETTINGS:
				synchronized (settings.getSyncedSettings().getSyncedModuleSettings()) {
					Set<SyncedModuleSettings> syncedModuleSettingsList = this.settings.getSyncedSettings()
							.getSyncedModuleSettings();
					for (Object object : data) {
						SyncedModuleSettings syncedModuleSettings = (SyncedModuleSettings) object;
						syncedModuleSettingsList.remove(syncedModuleSettings);
						persistence.removeEntity(syncedModuleSettings.getPersistenceID(), syncedModuleSettings.getClass());
						//TODO: EMIT THIS EVENT?
					}
				}
				break;
			default:
				logger.debug("Unexpected type of MessageInfo: \"" + info.getType() + "\"");
				break;

		}
	}
}
