package de.qabel.box.storage;


import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNameConflict;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.core.config.Contact;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import kotlin.jvm.functions.Function0;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.meanbean.util.AssertionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public abstract class BoxVolumeTest {
    private static final Logger logger = LoggerFactory.getLogger(BoxVolumeTest.class);
    private final String DEFAULT_UPLOAD_FILENAME = "foobar";

    protected BoxVolume volume;
    protected BoxVolume volume2;
    protected byte[] deviceID;
    protected byte[] deviceID2;
    protected QblECKeyPair keyPair;
    protected final String bucket = "qabel";
    protected String prefix = UUID.randomUUID().toString();
    private String testFileName = "src/test/java/de/qabel/box/storage/testFile.txt";
    protected Contact contact;
    protected File volumeTmpDir;

    @Before
    public void setUp() throws IOException, QblStorageException {
        if (!new File(testFileName).exists()) {
            testFileName = "box/" + testFileName;
        }

        CryptoUtils utils = new CryptoUtils();
        deviceID = utils.getRandomBytes(16);
        deviceID2 = utils.getRandomBytes(16);
        volumeTmpDir = Files.createTempDirectory("qbl_test").toFile();

        keyPair = new QblECKeyPair(Hex.decode("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a"));
        contact = new Contact("contact", new LinkedList<DropURL>(), new QblECKeyPair().getPub());

        setUpVolume();

        volume.createIndex(bucket, prefix);
    }

    protected abstract StorageReadBackend getReadBackend();

    protected abstract void setUpVolume() throws IOException;

    @After
    public void cleanUp() throws IOException {
        cleanVolume();
        FileUtils.deleteDirectory(volumeTmpDir);
    }

    protected abstract void cleanVolume() throws IOException;

    @Test
    public void testCleansUpTmpUploads() throws Exception {
        BoxNavigation nav = volume.navigate();
        uploadFile(nav);

        assertNoTmpFiles();
    }

    private void assertNoTmpFiles() {
        List<File> nonDmFiles = new LinkedList<>();
        for (File file : volumeTmpDir.listFiles()) {
            if (file.getName().startsWith("dir")) {
                continue;   // allow DM tmp files for now cause we don't have a strategy to clean them
            }
            nonDmFiles.add(file);
        }

        if (!nonDmFiles.isEmpty()) {
            String message = "tmp dir was not cleaned: \n";
            for (File file : nonDmFiles) {
                message += file.getAbsolutePath() + "\n";
            }
            fail(message);
        }
    }

    @Test
    public void testCleansUpTmpDownloads() throws Exception {
        BoxNavigation nav = volume.navigate();
        BoxFile upload = uploadFile(nav);
        nav.download(upload).close();

        assertNoTmpFiles();
    }

    @Test
    public void testCreateIndex() throws QblStorageException {
        BoxNavigation nav = volume.navigate();
        assertThat(nav.listFiles().size(), is(0));
    }

    @Test
    public void testUploadFile() throws QblStorageException, IOException {
        uploadFile(volume.navigate());
    }

    @Test
    public void modifiedStateIsClearedOnCommit() throws Exception {
        IndexNavigation nav = volume.navigate();
        nav.setAutocommit(false);
        uploadFile(nav);
        assertFalse(nav.isUnmodified());
        nav.commit();
        assertTrue(nav.isUnmodified());
    }

    @Test(expected = QblStorageNotFound.class)
    public void testDeleteFile() throws QblStorageException, IOException {
        BoxNavigation nav = volume.navigate();
        BoxFile boxFile = uploadFile(nav);
        nav.delete(boxFile);
        nav.download(boxFile);
    }

    @Test
    public void uploadsStreams() throws Exception {
        InputStream in = new ByteArrayInputStream("testContent".getBytes());
        Long size = 11L;

        DefaultIndexNavigation nav = (DefaultIndexNavigation) volume.navigate();
        nav.setTime(new Function0<Long>() { @Override public Long invoke() { return 1234567890L; }}); // imagine lambda
        BoxFile file = nav.upload("streamedFile", in, size);
        InputStream out = volume2.navigate().download("streamedFile");

        assertEquals((Long)11L, file.getSize());
        assertEquals((Long)1234567890L, file.getMtime());
        assertEquals("testContent", new String(IOUtils.toByteArray(out)));
    }

    @Test
    public void hasUsefulDefaultTimeProvider() throws Exception {
        BoxFile file = volume.navigate().upload("a", new ByteArrayInputStream("x".getBytes()), 1L);
        assertThat(System.currentTimeMillis() - file.getMtime(), lessThan(10000L));
        assertThat(System.currentTimeMillis() - file.getMtime(), greaterThanOrEqualTo(0L));
    }

    private BoxFile uploadFile(BoxNavigation nav) throws QblStorageException, IOException {
        String filename = DEFAULT_UPLOAD_FILENAME;
        return uploadFile(nav, filename);
    }

    private BoxFile uploadFile(BoxNavigation nav, String filename) throws QblStorageException, IOException {
        File file = new File(testFileName);
        BoxFile boxFile = nav.upload(filename, file);
        BoxNavigation nav_new = volume.navigate();
        checkFile(boxFile, nav_new);
        return boxFile;
    }

    private void checkFile(BoxFile boxFile, BoxNavigation nav) throws QblStorageException, IOException {
        try (InputStream dlStream = nav.download(boxFile)) {
            assertNotNull("Download stream is null", dlStream);
            byte[] dl = IOUtils.toByteArray(dlStream);
            File file = new File(testFileName);
            assertThat(dl, is(Files.readAllBytes(file.toPath())));
        }
    }

    @Test
    public void hashIsCalculatedOnUpload() throws Exception {
        volume.getConfig().setDefaultHashAlgorithm("SHA-1");
        IndexNavigation nav = volume.navigate();
        BoxFile file = uploadFile(nav, "testfile");
        assertTrue(file.isHashed());
        assertEquals("a23818f6a36f37ded50028f8fe008b0473cc7416", Hex.toHexString(file.getHashed().getHash()));
    }

    @Test
    public void defaultsToBlake2bInDm() throws Exception {
        uploadFile(volume.navigate(), "testfile");
        Hash hash = volume2.navigate().getFile("testfile").getHashed();
        assertEquals(
            "0f23d0a7f6ed44055ccf2e6cd4e088211659699640bc25de5f99dbfe082410bd632dca3e35925d9dffa20ca9f99ea55c63c1b21591eccde907bd3de275c74147",
            Hex.toHexString(hash.getHash())
        );
        assertEquals("Blake2b", hash.getAlgorithm());
    }

    @Test
    public void testCreateFolder() throws QblStorageException, IOException {
        BoxNavigation nav = volume.navigate();
        BoxFolder boxFolder = nav.createFolder("foobdir");

        BoxNavigation folder = nav.navigate(boxFolder);
        assertNotNull(folder);
        BoxFile boxFile = uploadFile(folder);

        BoxNavigation folder_new = nav.navigate(boxFolder);
        checkFile(boxFile, folder_new);

        BoxNavigation nav_new = volume.navigate();
        List<BoxFolder> folders = nav_new.listFolders();
        assertThat(folders.size(), is(1));
        assertThat(boxFolder, equalTo(folders.get(0)));
    }

    @Test
    public void testAutocommitDelay() throws Exception {
        BoxNavigation nav = volume.navigate();
        nav.setAutocommit(true);
        nav.setAutocommitDelay(1000);
        BoxFile file = uploadFile(nav, "testfile");

        BoxNavigation nav2 = volume2.navigate();
        assertFalse(nav2.hasFile("testfile"));
        waitUntil(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                BoxNavigation nav3 = volume2.navigate();
                nav3.refresh();
                return nav3.hasFile("testfile");
            }
        }, 2000L);
    }

    private static void waitUntil(Callable<Boolean> evaluate, long timeout) {
        long startTime = System.currentTimeMillis();
        try {
            while (!evaluate.call()) {
                Thread.yield();
                Thread.sleep(10);
                if (System.currentTimeMillis() - timeout > startTime) {
                    fail("failed to wait...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void simpleDeleteFolder() throws Exception {
        BoxNavigation nav = volume.navigate();
        BoxFolder boxFolder = nav.createFolder("newfolder");
        nav.delete(boxFolder);
    }

    @Test
    public void testDeleteFolder() throws QblStorageException, IOException {
        BoxNavigation nav = volume.navigate();
        BoxFolder boxFolder = nav.createFolder("foobdir");

        BoxNavigation folder = nav.navigate(boxFolder);
        BoxFile boxFile = uploadFile(folder);
        BoxFolder subfolder = folder.createFolder("subfolder");

        nav.delete(boxFolder);
        BoxNavigation nav_after = volume.navigate();
        assertThat(nav_after.listFolders().isEmpty(), is(true));
        checkDeleted(boxFolder, subfolder, boxFile, nav_after);
    }

    private void checkDeleted(BoxFolder boxFolder, BoxFolder subfolder, BoxFile boxFile, BoxNavigation nav) throws QblStorageException {
        try {
            nav.download(boxFile);
            AssertionUtils.fail("Could download file in deleted folder");
        } catch (QblStorageNotFound e) {
        }
        try {
            nav.navigate(boxFolder);
            AssertionUtils.fail("Could navigate to deleted folder");
        } catch (QblStorageNotFound e) {
        }
        try {
            nav.navigate(subfolder);
            AssertionUtils.fail("Could navigate to deleted subfolder");
        } catch (QblStorageNotFound e) {
        }
    }

    @Test(expected = QblStorageNameConflict.class)
    public void testOverwriteFileError() throws QblStorageException, IOException {
        BoxNavigation nav = volume.navigate();
        uploadFile(nav);
        uploadFile(nav);
    }

    @Test
    public void testOverwriteFile() throws QblStorageException, IOException {
        BoxNavigation nav = volume.navigate();
        File file = new File(testFileName);
        nav.upload(DEFAULT_UPLOAD_FILENAME, file);
        nav.overwrite(DEFAULT_UPLOAD_FILENAME, file);
        assertThat(nav.listFiles().size(), is(1));
    }

    @Test
    public void testConflictFileUpdate() throws QblStorageException, IOException {
        BoxNavigation nav = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();
        File file1 = new File(testFileName);
        File file2 = tmpFileWithSize(0L);
        try {
            nav2.upload(DEFAULT_UPLOAD_FILENAME, file1);
            nav.upload(DEFAULT_UPLOAD_FILENAME, file2);
            nav2.commit();
            nav.commit();
            assertThat(nav.listFiles().size(), is(2));
            assertThat(nav.getFile(DEFAULT_UPLOAD_FILENAME).getSize(), is(0L)); // file2 gets the name
            assertThat(nav.getFile(DEFAULT_UPLOAD_FILENAME + "_conflict").getSize(), is(file1.length())); // file1 renamed
        } finally {
            file2.delete();
        }
    }

    private File tmpFileWithSize(Long size) throws IOException {
        Path file = Files.createTempFile("qbltmp", ".deleteme");
        StringBuffer content = new StringBuffer();
        for (int i = 0; i < size; i++) {
            content.append("1");
        }
        Files.write(file, content.toString().getBytes());
        return  file.toFile();
    }

    @Test
    public void testSuccessiveConflictFileUpdate() throws QblStorageException, IOException {
        BoxNavigation nav = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();
        File file1 = new File(testFileName);
        File file2 = tmpFileWithSize(2L);
        File file3 = tmpFileWithSize(3L);
        try {
            nav2.upload(DEFAULT_UPLOAD_FILENAME, file1);
            nav2.upload(DEFAULT_UPLOAD_FILENAME + "_conflict", file2);
            nav.upload(DEFAULT_UPLOAD_FILENAME, file3);
            nav2.commit();
            nav.commit();
            assertThat(nav.listFiles().size(), is(3));
            assertThat(nav.getFile(DEFAULT_UPLOAD_FILENAME).getSize(), is(3L)); // file3 gets the name
            assertThat(
                nav.getFile(DEFAULT_UPLOAD_FILENAME + "_conflict_conflict").getSize(),
                is(file1.length())
            ); // file1 renamed
        } finally {
            file2.delete();
            file3.delete();
        }
    }

    @Test
    public void testFoldersAreMergedOnConflict() throws Exception {
        BoxNavigation nav = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();

        nav.createFolder("folder1");
        nav2.createFolder("folder2");

        nav2.commit();
        nav.commit();
        nav.refresh();
        assertThat(nav.listFolders().size(), is(2));
    }

    private BoxNavigation setupConflictNav1() throws QblStorageException {
        BoxNavigation nav = volume.navigate();
        nav.setAutocommit(false);
        return nav;
    }

    @Test
    public void testDeletedFoldersAreMergedOnConflict() throws Exception {
        BoxNavigation nav = setupConflictNav1();
        BoxFolder folder1 = nav.createFolder("folder1");
        nav.commit();

        BoxNavigation nav2 = setupConflictNav2();
        BoxFolder folder2 = nav2.createFolder("folder2");
        nav2.commit();
        nav = setupConflictNav1();

        nav2.delete(folder2);
        nav.delete(folder1);
        nav2.commit();
        nav.commit();

        nav.setMetadata(nav.reloadMetadata());
        assertFalse(nav.hasFolder("folder2"));
        assertFalse(nav.hasFolder("folder1"));
    }

    @Test
    public void testDeletedFilesAreMergedOnConflict() throws Exception {
        BoxNavigation nav = setupConflictNav1();
        BoxFile file1 = uploadFile(nav, "file1");
        nav.commit();
        getReadBackend().download("blocks/" + file1.getBlock()).close();

        BoxNavigation nav2 = setupConflictNav2();
        BoxFile file2 = uploadFile(nav2, "file2");
        nav2.commit();
        getReadBackend().download("blocks/" + file2.getBlock()).close();
        nav = setupConflictNav1();

        nav2.delete(file2);
        nav.delete(file1);
        nav2.commit();
        nav.commit();

        nav.setMetadata(nav.reloadMetadata());
        assertFalse(nav.hasFile("file1"));
        assertFalse(nav.hasFile("file2"));

        assertFileBlockDeleted(file1);
        assertFileBlockDeleted(file2);
    }

    private void assertFileBlockDeleted(BoxFile file2) throws IOException, QblStorageException {
        try {
            getReadBackend().download("blocks/" + file2.getBlock()).close();
            fail("block of file " + file2.getName() + " was not deleted");
        } catch (QblStorageNotFound ignored) {
        }
    }

    @Test
    public void testFileNameConflict() throws QblStorageException {
        BoxNavigation nav = volume.navigate();
        nav.createFolder(DEFAULT_UPLOAD_FILENAME);
        nav.upload(DEFAULT_UPLOAD_FILENAME, new File(testFileName));
        assertTrue(nav.hasFolder(DEFAULT_UPLOAD_FILENAME + "_conflict"));
        assertTrue(nav.hasFile(DEFAULT_UPLOAD_FILENAME));
    }

    @Test(expected = QblStorageNameConflict.class)
    public void testFolderNameConflict() throws QblStorageException {
        BoxNavigation nav = volume.navigate();
        nav.upload(DEFAULT_UPLOAD_FILENAME, new File(testFileName));
        nav.createFolder(DEFAULT_UPLOAD_FILENAME);
    }

    @Test
    public void testNameConflictOnDifferentClients() throws QblStorageException, IOException {
        BoxNavigation nav = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();
        File file = new File(testFileName);
        nav2.createFolder(DEFAULT_UPLOAD_FILENAME);
        nav.upload(DEFAULT_UPLOAD_FILENAME, file);
        nav2.commit();
        nav.commit();
        assertThat(nav.listFiles().size(), is(1));
        assertThat(nav.listFolders().size(), is(1));
        assertThat(nav.listFiles().get(0).name, is("foobar"));
        assertThat(nav.listFolders().get(0).name, startsWith("foobar_conflict"));
    }

    @Test
    public void testAddsShareToIndexWhenShareIsCreated() throws Exception {
        IndexNavigation index = volume.navigate();
        index.createFolder("subfolder");
        BoxNavigation nav = index.navigate("subfolder");
        BoxFile file = nav.upload(DEFAULT_UPLOAD_FILENAME, new File(testFileName));

        nav.share(keyPair.getPub(), file, "receiverId");

        assertThat(index.listShares().size(), is(1));
    }

    @Test
    public void testFolderNameConflictOnDifferentClients() throws QblStorageException, IOException {
        BoxNavigation nav = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();
        File file = new File(testFileName);
        nav2.upload(DEFAULT_UPLOAD_FILENAME, file);
        nav.createFolder(DEFAULT_UPLOAD_FILENAME);
        nav2.commit();
        nav.commit();
        assertThat(nav.listFiles().size(), is(1));
        assertThat(nav.listFolders().size(), is(1));
        // folders are merged
        assertThat(nav.listFiles().get(0).name, equalTo("foobar"));
    }

    private BoxNavigation setupConflictNav2() throws QblStorageException {
        BoxNavigation nav2 = volume2.navigate();
        nav2.setAutocommit(false);
        return nav2;
    }

    @Test
    public void testShare() throws Exception {
        BoxNavigation nav = volume.navigate();
        File file = new File(testFileName);
        BoxFile boxFile = nav.upload("file1", file);
        nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());

        BoxNavigation nav2 = volume2.navigate();
        BoxFile boxFile2 = nav2.getFile("file1");
        assertNotNull(boxFile2.getMeta());
        assertNotNull(boxFile2.getMetakey());
        assertEquals(boxFile.getMeta(), boxFile2.getMeta());
        assertTrue(Arrays.equals(boxFile.getMetakey(), boxFile2.getMetakey()));
        assertTrue(boxFile2.isShared());
        assertEquals(1, nav2.getSharesOf(boxFile2).size());
        assertEquals(contact.getKeyIdentifier(), nav2.getSharesOf(boxFile2).get(0).getRecipient());
        assertEquals(boxFile.getRef(), nav2.getSharesOf(boxFile2).get(0).getRef());
        assertEquals(Hex.toHexString(boxFile.getHashed().getHash()), Hex.toHexString(boxFile2.getHashed().getHash()));
    }

    @Test
    public void testShareUpdate() throws Exception {
        BoxNavigation nav = volume.navigate();
        File file = new File(testFileName);
        BoxFile boxFile = nav.upload("file1", file);
        nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());

        BoxFile updatedBoxFile = nav.overwrite("file1", file);
        assertEquals(boxFile.getMeta(), updatedBoxFile.getMeta());
        assertArrayEquals(boxFile.getMetakey(), updatedBoxFile.getMetakey());

        BoxNavigation nav2 = volume2.navigate();
        BoxFile boxFile2 = nav2.getFile("file1");
        assertNotNull(boxFile2.getMeta());
        assertNotNull(boxFile2.getMetakey());
        assertEquals(boxFile.getMeta(), boxFile2.getMeta());
        assertTrue(Arrays.equals(boxFile.getMetakey(), boxFile2.getMetakey()));
        assertTrue(boxFile2.isShared());
        assertEquals(1, nav2.getSharesOf(boxFile2).size());
        assertEquals(contact.getKeyIdentifier(), nav2.getSharesOf(boxFile2).get(0).getRecipient());
        assertEquals(updatedBoxFile.getRef(), nav2.getSharesOf(boxFile2).get(0).getRef());

        FileMetadata fm = nav2.getFileMetadata(boxFile);
        BoxExternalFile externalFile = fm.getFile();
        assertEquals("the file metadata have not been updated", updatedBoxFile.getBlock(), externalFile.getBlock());
    }

    private File download(InputStream in) throws IOException {
        Path path = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "tmpdownload", "");
        Files.write(path, IOUtils.toByteArray(in));
        return path.toFile();
    }

    @Test
    public void testUnshare() throws Exception {
        BoxNavigation nav = volume.navigate();
        File file = new File(testFileName);
        BoxFile boxFile = nav.upload("file1", file);
        nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());
        nav.unshare(boxFile);

        BoxNavigation nav2 = volume2.navigate();
        BoxFile boxFile2 = nav2.getFile("file1");
        assertNull(boxFile2.getMeta());
        assertNull(boxFile2.getMetakey());
        assertFalse(boxFile2.isShared());
        assertEquals(0, nav2.getSharesOf(boxFile2).size());
    }

    @Test
    public void deleteCleansShares() throws Exception {
        BoxNavigation nav = volume.navigate();
        File file = new File(testFileName);
        BoxFile boxFile = nav.upload("file1", file);
        nav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());
        String prefix = boxFile.getPrefix();
        String meta = boxFile.getMeta();
        byte[] metakey = boxFile.getMetakey();
        assertTrue(blockExists(meta));
        assertFalse(nav.getSharesOf(boxFile).isEmpty());

        nav.delete(boxFile);
        assertNull(boxFile.getMeta());
        assertNull(boxFile.getMetakey());

        // file metadata has been deleted
        assertFalse(blockExists(meta));

        // share has been removed from index
        boxFile.setShared(Share.create(meta, metakey));
        assertTrue(nav.getSharesOf(boxFile).isEmpty());
    }

    @Test
    public void folderConflictsArePreventedByPessimisticCommits() throws Exception {
        // set up navs that would be autocommitted after they have content (and thus overwrite each other)
        BoxNavigation nav1 = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();
        nav1.setAutocommit(true);
        nav1.setAutocommitDelay(2000L);
        nav2.setAutocommit(true);
        nav2.setAutocommitDelay(2000L);

        // add conflicting folders simultaneously
        BoxFolder folder1 = nav1.createFolder("folder");
        BoxFolder folder2 = nav2.createFolder("folder");
        BoxNavigation subnav1 = nav1.navigate(folder1);
        BoxNavigation subnav2 = nav2.navigate(folder2);

        // add content simultaneously
        File file = new File(testFileName);
        subnav1.upload("file1", file);
        subnav2.upload("file2", file);

        // make sure they commit (and conflict) now
        nav1.commit();
        subnav1.commit();
        nav2.commit();
        subnav2.commit();

        // test the conflict result
        nav1.refresh();
        assertThat(nav1.listFolders(), hasSize(1));

        BoxNavigation subnav = nav1.navigate(folder1);
        assertTrue(subnav.hasFile("file1"));
        assertTrue(subnav.hasFile("file2"));
        assertThat(subnav.listFiles(), hasSize(2));
    }

    @Test
    public void sameFilesAreMerged() throws Exception {
        // set up navs that would be autocommitted after they have content (and thus overwrite each other)
        BoxNavigation nav1 = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();

        // add content simultaneously
        File file = new File(testFileName);
        nav1.upload("c", file);
        nav2.upload("c", file);

        nav1.commit();
        nav2.commit();

        // test the conflict result
        nav1.refresh();
        assertTrue(nav1.hasFile("c"));
        nav2.refresh();
        assertTrue(nav1.hasFile("c"));
        assertFalse("Conflict file for 'c' found where none should be", nav1.hasFile("c_conflict"));
        assertThat(nav1.listFiles(), hasSize(1));
    }

    @Test
    public void conflictsBySameNameInSubfolders() throws Exception {
        // set up navs that would be autocommitted after they have content (and thus overwrite each other)
        BoxNavigation nav1 = setupConflictNav1();
        BoxNavigation nav2 = setupConflictNav2();

        // add conflicting folders simultaneously
        BoxFolder folder1 = nav1.createFolder("a");
        BoxFolder folder2 = nav2.createFolder("a");
        BoxNavigation subnav1 = nav1.navigate(folder1);
        BoxNavigation subnav2 = nav2.navigate(folder2);

        BoxFolder folderB1 = subnav1.createFolder("b");
        BoxFolder folderB2 = subnav2.createFolder("b");
        BoxNavigation subnavB1 = subnav1.navigate(folderB1);
        BoxNavigation subnavB2 = subnav2.navigate(folderB2);

        // add content simultaneously
        File file = new File(testFileName);
        subnavB1.upload("c", file);
        subnavB2.upload("c", file);

        nav1.commit();
        subnav1.commit();
        subnavB1.commit();
        nav2.commit();
        subnav2.commit();
        subnavB2.commit();

        // test the conflict result
        nav1.refresh();
        assertThat(nav1.listFolders(), hasSize(1));
        subnav1.refresh();
        assertThat(subnav1.listFolders(), hasSize(1));

        subnavB1.refresh();
        assertTrue(subnavB1.hasFile("c"));
        subnavB2.refresh();
        assertTrue(subnavB2.hasFile("c"));
        assertFalse("Conflict file for 'c' found where none should be", subnavB1.hasFile("c_conflict"));
        assertFalse("Conflict file for 'c' found where none should be", subnavB2.hasFile("c_conflict"));
        assertThat(subnavB1.listFiles(), hasSize(1));
        assertThat(subnavB2.listFiles(), hasSize(1));
    }

    @Test
    public void shareInsertedInIndexNavigationWhenSharingFromFolder() throws Exception {
        BoxNavigation nav = volume.navigate();
        nav.setAutocommit(false);
        BoxFolder folder = nav.createFolder("folder");
        BoxNavigation subNav = nav.navigate(folder);
        File file = new File(testFileName);
        BoxFile boxFile = subNav.upload("file1", file);
        subNav.share(keyPair.getPub(), boxFile, contact.getKeyIdentifier());
        subNav.commit();

        BoxNavigation nav2 = volume2.navigate().navigate("folder");
        assertThat(nav2.getSharesOf(nav2.getFile("file1")), hasSize(1));

    }

    private String originalRootRef() throws QblStorageException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new QblStorageException(e);
        }
        md.update(prefix.getBytes());
        md.update(keyPair.getPrivateKey());
        byte[] digest = md.digest();
        byte[] firstBytes = Arrays.copyOfRange(digest, 0, 16);
        ByteBuffer bb = ByteBuffer.wrap(firstBytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    @Test
    public void rootRefIsCompatible() throws Exception {
        assertThat(originalRootRef(), equalTo(volume.getRootRef()));
    }

    protected boolean blockExists(String meta) throws QblStorageException {
        try {
            getReadBackend().download(meta);
            return true;
        } catch (QblStorageNotFound e) {
            return false;
        }
    }
}
