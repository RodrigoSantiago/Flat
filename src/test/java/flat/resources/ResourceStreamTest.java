package flat.resources;

import flat.uxml.UXNode;
import flat.uxml.UXSheet;
import flat.window.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, ResourcesManager.class})
public class ResourceStreamTest {

    ResourcesManager manager;
    @Before
    public void before() {
        mockStatic(Application.class);
        mockStatic(ResourcesManager.class);
        manager = mock(ResourcesManager.class);
        when(Application.getResourcesManager()).thenReturn(manager);
    }

    @Test
    public void constructor() {
        ResourceStream stream = new ResourceStream("fileName");
        assertEquals("/fileName", stream.getResourceName());

        when(manager.isFolder("/folder/path")).thenReturn(true);
        ResourceStream folder = new ResourceStream("folder/path");
        assertEquals("/folder/path/", folder.getResourceName());
    }

    @Test
    public void relativeFile() {
        ResourceStream file = new ResourceStream("/file/path/name");
        assertEquals("/file/path/name", file.getResourceName());
        assertFalse(file.isFolder());

        ResourceStream rootFile = new ResourceStream("/file");
        assertEquals("/file", rootFile.getResourceName());
        assertFalse(rootFile.isFolder());

        ResourceStream relative = file.getRelative("other/path");
        assertEquals("/file/path/other/path", relative.getResourceName());

        ResourceStream relativeRoot = rootFile.getRelative("other/path");
        assertEquals("/other/path", relativeRoot.getResourceName());
    }

    @Test
    public void relativeFileBackwards() {
        ResourceStream file = new ResourceStream("/file/path/name");
        assertEquals("/file/path/name", file.getResourceName());
        assertFalse(file.isFolder());

        ResourceStream rootFile = new ResourceStream("/file");
        assertEquals("/file", rootFile.getResourceName());
        assertFalse(rootFile.isFolder());

        ResourceStream relative = file.getRelative("../relative");
        assertEquals("/file/relative", relative.getResourceName());

        ResourceStream relativeRoot = rootFile.getRelative("../relative");
        assertEquals("/relative", relativeRoot.getResourceName());

        ResourceStream tooMuchRelative = file.getRelative("../../../../../../../relative");
        assertEquals("/relative", tooMuchRelative.getResourceName());
    }

    @Test
    public void relativeFolder() {
        when(manager.isFolder(any())).thenReturn(true);

        ResourceStream folder = new ResourceStream("/folder/path/name");
        assertEquals("/folder/path/name/", folder.getResourceName());
        assertTrue(folder.isFolder());

        ResourceStream rootFolder = new ResourceStream("/folder");
        assertEquals("/folder/", rootFolder.getResourceName());
        assertTrue(rootFolder.isFolder());

        ResourceStream relative = folder.getRelative("other/path");
        assertEquals("/folder/path/name/other/path/", relative.getResourceName());

        ResourceStream relativeRoot = rootFolder.getRelative("other/path");
        assertEquals("/folder/other/path/", relativeRoot.getResourceName());
    }

    @Test
    public void relativeFolderBackwards() {
        when(manager.isFolder(any())).thenReturn(true);

        ResourceStream folder = new ResourceStream("/folder/path/name");
        assertEquals("/folder/path/name/", folder.getResourceName());
        assertTrue(folder.isFolder());

        ResourceStream rootFolder = new ResourceStream("/folder");
        assertEquals("/folder/", rootFolder.getResourceName());
        assertTrue(rootFolder.isFolder());

        ResourceStream relative = folder.getRelative("../relative/path");
        assertEquals("/folder/path/relative/path/", relative.getResourceName());

        ResourceStream relativeRoot = rootFolder.getRelative("../relative/path");
        assertEquals("/relative/path/", relativeRoot.getResourceName());

        ResourceStream tooMuchRelative = folder.getRelative("../../../../../../../relative/path");
        assertEquals("/relative/path/", tooMuchRelative.getResourceName());
    }
}