package com.limegroup.gnutella.xml;

import com.limegroup.gnutella.*;
import com.limegroup.gnutella.util.*;
import com.limegroup.gnutella.messages.*;
import com.limegroup.gnutella.mp3.ID3Reader;
import java.io.*;
import com.sun.java.util.collections.*;

/**
 * This class handles querying shared files with XML data and returning XML data
 * in replies.
 */
public class MetaFileManager extends FileManager {
    
    /**
     * Lock used when loading meta-settings.
     */
    private final Object META_LOCK = new Object();
    
    /**
     * Overrides FileManager.query.
     *
     * Used to search XML information in addition to normal searches.
     */
    public synchronized Response[] query(QueryRequest request) {
        Response[] result = super.query(request);

        if (shouldIncludeXMLInResponse(request)) {
            LimeXMLDocument doc = request.getRichQuery();
            if( doc != null ) {
                Response[] metas = RichQueryHandler.instance().query(doc);
                if (metas != null) // valid query & responses.
                    result = union(result, metas);
            }
        }
        
        return result;
    }
    
    /**
     * Returns whether or not a response to this query should include XML.
     * Currently only includes XML if the request desires it or
     * if the request wants an out of band reply.
     */
    protected boolean shouldIncludeXMLInResponse(QueryRequest qr) {
        return qr.desiresXMLResponses() || 
               qr.desiresOutOfBandReplies();
    }
    
    /**
     * Adds XML to the response.  This assumes that shouldIncludeXMLInResponse
     * was already consulted and returned true.
     *
     * If the FileDesc has no XMLDocuments, this does nothing.
     * If the FileDesc has one XML Document, this sets it as the response doc.
     * If the FileDesc has multiple XML Documents, this does nothing.
     * The reasoning behind not setting the document when there are multiple
     * XML docs is that presumably the query will be a 'rich' query,
     * and we want to include only the schema that was in the query.
     * 
     * @param response the <tt>Response</tt> instance that XML should be 
     *  added to 
     * @param fd the <tt>FileDesc</tt> that provides access to the 
     *   <tt>LimeXMLDocuments</tt> to add to the response
     */
    protected void addXMLToResponse(Response response, FileDesc fd) {
        List docs = fd.getLimeXMLDocuments();
        if( docs.size() == 0 )
            return;
        if( docs.size() == 1 )
            response.setDocument((LimeXMLDocument)docs.get(0));
    }
    
    /**
     * Notification that a file has changed.
     * This implementation is different than FileManager's
     * in that it maintains the XML.
     *
     * Important note: This method is called AFTER the file has
     * changed.  It is possible that the metadata we wanted to write
     * did not get written out completely.  We should NOT attempt
     * to add the old metadata again, because we may end up
     * recursing infinitely trying to write this metadata.
     * However, it isn't very robust to blindly assume that the only
     * metadata associated with this file was audio metadata.
     * So, we make use of the fact that addFileIfShared will only
     * add one type of metadata per file.  We read the ID3 tags off
     * the file and insert it first into the list, ensuring
     * that the existing metadata is the one that's added, short-circuiting
     * any infinite loops.
     */
    public FileDesc fileChanged(File f) {
        FileDesc fd = getFileDescForFile(f);
        if( fd == null )
            return null;
        // store the creation time for later re-input
        CreationTimeCache ctCache = CreationTimeCache.instance();
        Long cTime = ctCache.getCreationTime(fd.getSHA1Urn());

        List xmlDocs = fd.getLimeXMLDocuments();        
        if(LimeXMLUtils.isMP3File(f)) {
            try {
                LimeXMLDocument diskID3Doc = ID3Reader.readDocument(f);
                xmlDocs = resolveAudioDocs(xmlDocs,diskID3Doc);
            } catch(IOException e) {
                // if we were unable to read this document,
                // then simply add the file without metadata.
                return super.fileChanged(f);
            }
        }

        FileDesc removed = removeFile(f);        
        Assert.that(fd == removed, "did not remove valid fd.");
        _needRebuild = true;
        fd = addFile(f, xmlDocs);
        // file may not be shared anymore or may be installer file
        if ((fd != null) && (cTime != null)) { 
            //re-populate the ctCache
            synchronized (ctCache) {
                ctCache.removeTime(fd.getSHA1Urn());//addFile() put lastModified
                ctCache.addTime(fd.getSHA1Urn(), cTime.longValue());
                ctCache.commitTime(fd.getSHA1Urn());
            }
        }
        
        // Notify the GUI about the changes...
        FileManagerEvent evt = null;
        
        if (fd != null) {
            evt = new FileManagerEvent(this, 
                                       FileManagerEvent.CHANGE, 
                                       new FileDesc[]{removed,fd});
        } else {
            evt = new FileManagerEvent(this, 
                                       FileManagerEvent.REMOVE, 
                                       new FileDesc[]{removed});
        }
                                            
        RouterService.getCallback().handleFileManagerEvent(evt);
        
        return fd;
    }        
    
    /**
     * Finds the audio metadata document in allDocs, and makes it's id3 fields
     * identical with the fields of id3doc (which are only id3).
     */
    private List resolveAudioDocs(List allDocs, LimeXMLDocument id3Doc) {
        LimeXMLDocument audioDoc = null;
        LimeXMLSchema audioSchema = 
        LimeXMLSchemaRepository.instance().getSchema(ID3Reader.schemaURI);
        
        for(Iterator iter = allDocs.iterator(); iter.hasNext() ;) {
            LimeXMLDocument doc = (LimeXMLDocument)iter.next();
            if(doc.getSchema() == audioSchema) {
                audioDoc = doc;
                break;
            }
        }
        if(id3Doc.equals(audioDoc)) //No issue -- both documents are the same
            return allDocs; //did not modify list, keep using it
        
        List retList = new ArrayList();
        retList.addAll(allDocs);
        
        if(audioDoc == null) {//nothing to resolve
            retList.add(id3Doc);
            return retList;
        }
        
        //OK. audioDoc exists, remove it
        retList.remove(audioDoc);
        
        //now add the non-id3 tags from audioDoc to id3doc
        List audioList = null;
        List id3List = null;
        try {
            audioList = audioDoc.getOrderedNameValueList();
            id3List = id3Doc.getOrderedNameValueList();
        } catch (SchemaNotFoundException snfx) {
            ErrorService.error(snfx);
        }
        for(int i = 0; i < audioList.size(); i++) {
            NameValue nameVal = (NameValue)audioList.get(i);
            if(ID3Reader.isNonID3Field(nameVal.getName()))
                id3List.add(nameVal);
        }
        audioDoc = new LimeXMLDocument(id3List, ID3Reader.schemaURI);
        retList.add(audioDoc);
        return retList;
    }


    /**
     * Removes the LimeXMLDocuments associated with the removed
     * FileDesc from the various LimeXMLReplyCollections.
     */
    protected FileDesc removeFile(File f) {
        FileDesc fd = super.removeFile(f);
        // nothing removed, ignore.
        if( fd == null )
            return null;
            
        SchemaReplyCollectionMapper mapper =
            SchemaReplyCollectionMapper.instance();            
            
        //Get the schema URI of each document and remove from the collection
        // We must remember the schemas and then remove the doc, or we will
        // get a concurrent mod exception because removing the doc also
        // removes it from the FileDesc.
        List xmlDocs = fd.getLimeXMLDocuments();
        List schemas = new LinkedList();
        for(Iterator i = xmlDocs.iterator(); i.hasNext(); )
            schemas.add( ((LimeXMLDocument)i.next()).getSchemaURI() );
        for(Iterator i = schemas.iterator(); i.hasNext(); ) {
            String uri = (String)i.next();
            LimeXMLReplyCollection col = mapper.getReplyCollection(uri);
            if( col != null )
                col.removeDoc( fd );
        }
        _needRebuild = true;
        return fd;
    }

    /**
     * @modifies this
     * @effects calls addFileIfShared(file), then stores any metadata from the
     *  given XML documents.  metadata may be null if there is no data.  Returns
     *  the value from addFileIfShared.  Returns the value from addFileIfShared.
     *  <b>WARNING: this is a potential security hazard.</b> 
     *
     * @return The FileDesc that was added, or null if nothing added.
     */
    public FileDesc addFileIfShared(File file, List metadata) {
        
        // kicks off a FileManagerEvent.ADD event
        FileDesc fd1 = super.addFileIfShared(file);
        
        if (fd1 != null) {
            
            URN oldURN = fd1.getSHA1Urn();
            List oldDoc = fd1.getLimeXMLDocuments();
            int before = (oldDoc != null) ? oldDoc.size() : 0;
            
            // There's an loopback from LimeXMLReplyCollection.commitID3Data()
            // to MetaFileManager.fileChanged(). It's called whenever the URN 
            // changes (e.g. if the metadata list had more or better meta 
            // than the actual file)
            
            FileDesc fd2 = this.addFile(file, fd1, metadata);
            
            // OK, the URN didn't changed (i.e. no event through fileChanged())
            // but as we appended metadata to the FileDesc it changed from our
            // perspective ...
            
            if (fd2 != null) { // Note: cannot be null as fd1 == fd2
                
                // ... and only if meatadata was actually added (before should
                // be always 0 and now >= 0)
                
                List newDoc = fd2.getLimeXMLDocuments();
                int now = (newDoc != null) ? newDoc.size() : 0;

                if (before < now && oldURN.equals(fd2.getSHA1Urn()) == true) {
                    
                    FileManagerEvent evt = new FileManagerEvent(this, 
                                                    FileManagerEvent.CHANGE, 
                                                    new FileDesc[]{fd1, fd2});

                    RouterService.getCallback().handleFileManagerEvent(evt);
                }
            }
            
            return fd2;
        }
        
        return fd1;
    }
    
    /**
     * 
     * @return The FileDesc that was added, or null if nothing added.
     */
    protected FileDesc addFile(File file, List metadata) {
        
        FileDesc fd = super.addFile(file);
        
        if (fd != null) {
            return addFile(file, fd, metadata);
        }
        
        return fd;
    }
    
    /**
     * 
     * @return The FileDesc that was added, or null if nothing added.
     */
    private FileDesc addFile(File file, FileDesc fd, List metadata) {
        
        // if not added, exit.
        if( fd == null )
            return null;
            
        // if added, but no metadata, try and create some.
        if( metadata == null || metadata.size() == 0) {
            // not mp3, can't create any ... 
            if(!LimeXMLUtils.isMP3File(file))
                return fd;

            LimeXMLDocument doc;
            try {
                doc = ID3Reader.readDocument(file);
            } catch(IOException ioe) {
                // unable to read? oh well, no metadata.
                return fd;
            }
            // create a list of metadata and add the doc to it.
            metadata = new LinkedList();
            metadata.add(doc);
            // fall through and add it.
        }

        SchemaReplyCollectionMapper mapper =
            SchemaReplyCollectionMapper.instance();
        

        // add xml docs as appropriate, one per schema.
        List schemasAddedTo = new LinkedList();
        for(Iterator iter = metadata.iterator(); iter.hasNext(); ) {
            LimeXMLDocument currDoc = (LimeXMLDocument)iter.next();
            String uri = currDoc.getSchemaURI();
            LimeXMLReplyCollection collection = mapper.getReplyCollection(uri);
            if (collection != null && !schemasAddedTo.contains(uri)) {
                schemasAddedTo.add(uri);
                if( ID3Reader.isCorrupted(currDoc) )
                    currDoc = ID3Reader.fixCorruption(currDoc);
                collection.addReplyWithCommit(file, fd, currDoc, true);
                
            }
            
        }

        _needRebuild = true;
        return fd;
    }

    /**This method overrides FileManager.loadSettingsBlocking(), though
     * it calls the super method to load up the shared file DB.  Then, it
     * processes these files and annotates them automatically as apropos.
     * TODO2: Eventually we will think that its too much of a burden
     * to have this thread be blocking in which case we will have to 
     * have the load thread also handle the reloading of the meta-data.
     * Question: Do we really want to reload the meta-data whenever a we
     * want to update the file information?? It depends on how we want to 
     * handle the meta-data and its relation to the file system
     */
    protected void loadSettingsBlocking(boolean notifyOnClear){
		RouterService.getCallback().setAnnotateEnabled(false);
        // let FileManager do its work....
        super.loadSettingsBlocking(notifyOnClear);
        if (loadThreadInterrupted())
            return;
        synchronized(META_LOCK){
            SchemaReplyCollectionMapper mapper = 
                  SchemaReplyCollectionMapper.instance();
            //created maper schemaURI --> ReplyCollection
            LimeXMLSchemaRepository schemaRepository = 
                  LimeXMLSchemaRepository.instance();

            if (loadThreadInterrupted())
                return;

            //now the schemaRepository contains all the schemas.
            String[] schemas = schemaRepository.getAvailableSchemaURIs();
            //we have a list of schemas
            int len = schemas.length;
            LimeXMLReplyCollection collection;
            FileDesc fds[] = super.getAllSharedFileDescriptors();
            for(int i=0; i < len && !loadThreadInterrupted(); i++) {
                //One ReplyCollection per schema
                String s = LimeXMLSchema.getDisplayString(schemas[i]);
                collection = 
                    new LimeXMLReplyCollection(fds, schemas[i], 
                                           s.equalsIgnoreCase("audio"));
                //Note: the collection may have size==0!
                mapper.add(schemas[i],collection);
            }
            //showXMLData();
        }//end of synchronized block
		RouterService.getCallback().setAnnotateEnabled(true);
    }

    /**
     * Creates a new array, the size of which is less than or equal
     * to normals.length + metas.length.
     */
    private Response[] union(Response[] normals, Response[] metas){       
        if(normals == null)
            return metas;
        if(metas == null)
            return normals;
            
            
        // It is important to use a HashSet here so that duplicate
        // responses are not sent.
        // Unfortunately, it is still possible that one Response
        // did not have metadata but the other did, causing two
        // responses for the same file.
            
        Set unionSet = new HashSet();
        for(int i = 0; i < metas.length; i++)
            unionSet.add(metas[i]);
        for(int i = 0; i < normals.length; i++)
            unionSet.add(normals[i]);

        //The set contains all the elements that are the union of the 2 arrays
        Response[] retArray = new Response[unionSet.size()];
        retArray = (Response[])unionSet.toArray(retArray);
        return retArray;
    }

    /**
     * build the  QRT table
     * call to super.buildQRT and add XML specific Strings
     * to QRT
     */
    protected void buildQRT() {
        super.buildQRT();
        Iterator iter = getXMLKeyWords().iterator();
        while(iter.hasNext())
            _queryRouteTable.add((String)iter.next());
        
        iter = getXMLIndivisibleKeyWords().iterator();
        while(iter.hasNext())
            _queryRouteTable.addIndivisible((String)iter.next());
    }

    /**
     * Returns a list of all the words in the annotations - leaves out
     * numbers. The list also includes the set of words that is contained
     * in the names of the files.
     */
    private List getXMLKeyWords(){
        ArrayList words = new ArrayList();
        //Now get a list of keywords from each of the ReplyCollections
        SchemaReplyCollectionMapper map=SchemaReplyCollectionMapper.instance();
        LimeXMLSchemaRepository rep = LimeXMLSchemaRepository.instance();
        String[] schemas = rep.getAvailableSchemaURIs();
        LimeXMLReplyCollection collection;
        int len = schemas.length;
        for(int i=0;i<len;i++){
            collection = map.getReplyCollection(schemas[i]);
            if(collection==null)//not loaded? skip it and keep goin'
                continue;
            words.addAll(collection.getKeyWords());
        }
        return words;
    }
    

    /** @return A List of KeyWords from the FS that one does NOT want broken
     *  upon hashing into a QRT.  Initially being used for schema uri hashing.
     */
    private List getXMLIndivisibleKeyWords() {
        ArrayList words = new ArrayList();
        LimeXMLSchemaRepository rep = LimeXMLSchemaRepository.instance();
        String[] schemas = rep.getAvailableSchemaURIs();
        for (int i = 0; i < schemas.length; i++) 
            if (schemas[i] != null)
                words.add(schemas[i]);        
        return words;
    }

    /**
     * Used only for showing the current XML data in the system. This method
     * is used only for the purpose of testing. It is not used for anything 
     * else.
     */
     /*
    private void showXMLData(){
        //get all the schemas
        LimeXMLSchemaRepository rep = LimeXMLSchemaRepository.instance();
        String[] schemas = rep.getAvailableSchemaURIs();
        SchemaReplyCollectionMapper mapper = 
                SchemaReplyCollectionMapper.instance();
        int len = schemas.length;
        LimeXMLReplyCollection collection;
        for(int i=0; i<len; i++){
            System.out.println("Schema : " + schemas[i]);
            System.out.println("-----------------------");
            collection = mapper.getReplyCollection(schemas[i]);
            if (collection == null || collection.getCount()<1){
                System.out.println("No docs corresponding to this schema ");
                continue;
            }
            List replies = collection.getCollectionList();
            int size = replies.size();
            for(int j=0; j< size; j++){
                System.out.println("Doc number "+j);
                System.out.println("-----------------------");
                LimeXMLDocument doc = (LimeXMLDocument)replies.get(j);
                List elements = doc.getNameValueList();
                int t = elements.size();
                for(int k=0; k<t; k++){
                    NameValue nameValue = (NameValue)elements.get(k);
                    System.out.println("Name " + nameValue.getName());
                    System.out.println("Value " + nameValue.getValue());
                
                }
            }
        }
    } */

}

        

