package de.qabel.core.config;

import java.io.Serializable;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.qabel.ackack.Actor;
import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.Responsible;

/**
 * Actor which handles the access to the configuration of Qabel.
 *
 */
public class ConfigActor extends Actor {
	private static ConfigActor defaultConfigActor = null;
	private Settings settings;
	private static final String RETRIEVE_ACCOUNTS = "retrieveAccounts";
	private static final String RETRIEVE_DROPSERVERS = "retrieveDropServers";
	private static final String RETRIEVE_IDENTITIES = "retrieveIdentities";
	private static final String RETRIEVE_LOCALMODULESETTINGS = "retrieveLocalModuleSettings";
	private static final String RETRIEVE_LOCALSETTINGS = "retrieveLocalSettings";
	private static final String RETRIEVE_STORAGESERVERS = "retrieveStorageServers";
	private static final String RETRIEVE_STORAGEVOLUMES = "retrieveStorageVolumes";
	private static final String RETRIEVE_SYNCEDMODULESETTINGS = "retrieveSyncedModuleSettings";

	private static final String WRITE_ACCOUNTS = "writeAccounts";
	private static final String WRITE_DROPSERVERS = "writeDropServers";
	private static final String WRITE_IDENTITIES = "writeIdentities";
	private static final String WRITE_LOCALMODULESETTINGS = "writeLocalModuleSettings";
	private static final String WRITE_LOCALSETTINGS = "writeLocalSettings";
	private static final String WRITE_STORAGESERVERS = "writeStorageServers";
	private static final String WRITE_STORAGEVOLUMES = "writeStorageVolumes";
	private static final String WRITE_SYNCEDMODULESETTINGS = "writeSyncedModuleSettings";

	private static final String REMOVE_ACCOUNTS = "removeAccounts";
	private static final String REMOVE_DROPSERVERS = "removeDropServers";
	private static final String REMOVE_IDENTITIES = "removeIdentities";
	private static final String REMOVE_LOCALMODULESETTINGS = "removeLocalModuleSettings";
	private static final String REMOVE_STORAGESERVERS = "removeStorageServers";
	private static final String REMOVE_STORAGEVOLUMES = "removeStorageVolumes";
	private static final String REMOVE_SYNCEDMODULESETTINGS = "removeSyncedModuleSettings";

	private final static Logger logger = LogManager.getLogger(ConfigActor.class.getName());

	static ConfigActor getDefault() {
		if(defaultConfigActor == null) {
			defaultConfigActor = new ConfigActor(new Settings());
		}
		return defaultConfigActor;
	}

	public ConfigActor(Settings settings) {
		this.settings= settings;
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
		Accounts accounts;
		DropServers dropServers;
		Identities identities;
		Set<LocaleModuleSettings> localModuleSettingsList;
		StorageServers storageServers;
		StorageVolumes storageVolumes;
		Set<SyncedModuleSettings> syncedModuleSettingsList;

		switch(info.getType()) {
		case RETRIEVE_ACCOUNTS:
			info.response((Serializable[]) this.settings
					.getSyncedSettings().getAccounts().getAccounts()
					.toArray(new Account[0]));
			break;
		case RETRIEVE_DROPSERVERS:
			info.response((Serializable[]) this.settings
					.getSyncedSettings().getDropServers().getDropServers()
					.toArray(new DropServer[0]));
			break;
		case RETRIEVE_IDENTITIES:
			info.response((Serializable[]) this.settings
					.getSyncedSettings().getIdentities().getIdentities()
					.toArray(new Identity[0]));
			break;
		case RETRIEVE_LOCALMODULESETTINGS:
			info.response((Serializable[]) this.settings
					.getLocalSettings().getLocaleModuleSettings()
					.toArray(new LocaleModuleSettings[0]));
			break;
		case RETRIEVE_LOCALSETTINGS:
			info.response((Serializable) this.settings.getLocalSettings());
			break;
		case RETRIEVE_STORAGESERVERS:
			info.response((Serializable[]) this.settings
					.getSyncedSettings().getStorageServers()
					.getStorageServers().toArray(new StorageServer[0]));
			break;
		case RETRIEVE_STORAGEVOLUMES:
			info.response((Serializable[]) this.settings
					.getSyncedSettings().getStorageVolumes()
					.getStorageVolumes().toArray(new StorageVolume[0]));
			break;
		case RETRIEVE_SYNCEDMODULESETTINGS:
			info.response((Serializable[]) this.settings
					.getSyncedSettings().getSyncedModuleSettings()
					.toArray(new SyncedModuleSettings[0]));
			break;
		case WRITE_ACCOUNTS:
			accounts = this.settings.getSyncedSettings()
				.getAccounts();
			for(int i = 0; i< data.length; i++) {
				accounts.update((Account) data[i]);
			}
			break;
		case WRITE_DROPSERVERS:
			dropServers = this.settings.getSyncedSettings()
				.getDropServers();
			for(int i = 0; i< data.length; i++) {
				dropServers.update((DropServer) data[i]);
			}
			break;
		case WRITE_IDENTITIES:
			identities = this.settings.getSyncedSettings()
				.getIdentities();
			for(int i = 0; i< data.length; i++) {
				identities.replace((Identity) data[i]);
			}
			break;
		case WRITE_LOCALMODULESETTINGS:
			localModuleSettingsList = this.settings.getLocalSettings()
				.getLocaleModuleSettings();
			for(int i = 0; i< data.length; i++) {
				localModuleSettingsList.remove((LocaleModuleSettings) data[i]);
				localModuleSettingsList.add((LocaleModuleSettings) data[i]);
			}
			break;
		case WRITE_LOCALSETTINGS:
			this.settings.setLocalSettings((LocalSettings) data[0]);
			break;
		case WRITE_STORAGESERVERS:
			storageServers = this.settings.getSyncedSettings()
				.getStorageServers();
			for(int i = 0; i< data.length; i++) {
				storageServers.update((StorageServer) data[i]);
			}
			break;
		case WRITE_STORAGEVOLUMES:
			storageVolumes = this.settings.getSyncedSettings()
				.getStorageVolumes();
			for(int i = 0; i< data.length; i++) {
				storageVolumes.update((StorageVolume) data[i]);
			}
			break;
		case WRITE_SYNCEDMODULESETTINGS:
			syncedModuleSettingsList = this.settings.getSyncedSettings()
				.getSyncedModuleSettings();
			for(int i = 0; i< data.length; i++) {
				syncedModuleSettingsList.remove((SyncedModuleSettings) data[i]);
				syncedModuleSettingsList.add((SyncedModuleSettings) data[i]);
			}
			break;
		case REMOVE_ACCOUNTS:
			accounts = this.settings.getSyncedSettings().getAccounts();
			for(int i = 0; i< data.length; i++) {
				accounts.remove((Account) data[i]);
			}
			break;
		case REMOVE_DROPSERVERS:
			dropServers = this.settings.getSyncedSettings().getDropServers();
			for(int i = 0; i< data.length; i++) {
				dropServers.remove((DropServer) data[i]);
			}
			break;
		case REMOVE_IDENTITIES:
			identities = this.settings.getSyncedSettings().getIdentities();
			for(int i = 0; i< data.length; i++) {
				identities.remove((Identity) data[i]);
			}
			break;
		case REMOVE_LOCALMODULESETTINGS:
			localModuleSettingsList = this.settings.getLocalSettings()
				.getLocaleModuleSettings();
			for(int i = 0; i< data.length; i++) {
				localModuleSettingsList.remove((LocaleModuleSettings) data[i]);
			}
			break;
		case REMOVE_STORAGESERVERS:
			storageServers = this.settings.getSyncedSettings()
				.getStorageServers();
			for(int i = 0; i< data.length; i++) {
				storageServers.remove((StorageServer) data[i]);
			}
			break;
		case REMOVE_STORAGEVOLUMES:
			storageVolumes = this.settings.getSyncedSettings()
				.getStorageVolumes();
			for(int i = 0; i< data.length; i++) {
				storageVolumes.remove((StorageVolume) data[i]);
			}
			break;
		case REMOVE_SYNCEDMODULESETTINGS:
			syncedModuleSettingsList = this.settings.getSyncedSettings()
				.getSyncedModuleSettings();
			for(int i = 0; i< data.length; i++) {
				syncedModuleSettingsList.remove((SyncedModuleSettings) data[i]);
			}
			break;
		default:
			logger.debug("Unexpected type of MessageInfo: \"" + info.getType()
					+ "\"");
			break;
		}
		stop();
	}
}
