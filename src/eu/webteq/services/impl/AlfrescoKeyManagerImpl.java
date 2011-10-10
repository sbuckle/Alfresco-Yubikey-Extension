package eu.webteq.services.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.webteq.services.KeyManager;

public class AlfrescoKeyManagerImpl implements KeyManager {

	private static final Log logger = LogFactory.getLog(AlfrescoKeyManagerImpl.class);
	
	private Map<String, String> usernameKeyMap;
	private String path; // Path to database file in Alfresco relative to Company Home
	private Date lastModified;
	private NodeRef dbh;
	
	// Services
	protected FileFolderService fileFolderService;
	protected ContentService contentService;
	protected NodeService nodeService;
	protected SearchService searchService;
	
	public AlfrescoKeyManagerImpl() {
		usernameKeyMap = new HashMap<String, String>();
		dbh = null;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1970, 1, 1);
		lastModified = calendar.getTime();
	}
	
	public void setFileFolderService(FileFolderService folderService) {
		this.fileFolderService = folderService;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	private NodeRef getCompanyHome() {
		StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
		ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
		NodeRef companyHome = null;
		try {
			if (rs.length() == 0) {
				throw new AlfrescoRuntimeException("Could not find Company Home");
			}
			companyHome = rs.getNodeRef(0);
		} finally {
			rs.close();
		}
		return companyHome;
	}
	
	/**
	 * Resolves the device map to a node reference.
	 * Caches the reference to avoid looking it up each time.
	 * 
	 * @return NodeRef
	 */
	private NodeRef getRef() {
		if (dbh == null) {
			try {
				List<String> pathElements = Arrays.asList(getPath().split("/"));
				FileInfo fi = fileFolderService.resolveNamePath(getCompanyHome(), pathElements);
				dbh = fi.getNodeRef();
			} catch (FileNotFoundException fnfe) {
				logger.warn("Failed to initialise user/device map.");
				throw new AlfrescoRuntimeException("Could not open device map");
			}
		}
		return dbh;
	}
	
	private synchronized String getDeviceId(String username) {
		NodeRef db = getRef();
		Date modified = (java.util.Date)nodeService.getProperty(db, ContentModel.PROP_MODIFIED);
		if (lastModified.before(modified)) {
			try {
				ContentReader reader = contentService.getReader(db, ContentModel.PROP_CONTENT);
				String content = reader.getContentString();
				String[] entries = content.split("\r?\n");
				Map<String, String> newEntries = new HashMap<String, String>();
				for (int i = 0, len = entries.length; i < len; i++) {
					String entry = entries[i];
					if (entry.startsWith("#")) { continue; }
					String[] keyValue = entry.split(":");
					newEntries.put(keyValue[0], keyValue[1]);
				}
				lastModified = modified;
				usernameKeyMap = newEntries;
			} catch (Exception ex) {
				logger.warn("Failed to read database contents.");
			}
		}
		return usernameKeyMap.get(username);
	}
	
	@Override
	public boolean isOwner(String username, String deviceId) {
		String id = getDeviceId(username);
		if (id == null) return false;
		return deviceId.equals(id);
	}

	@Override
	public String getDevice(String username) {
		return getDeviceId(username);
	}

}
