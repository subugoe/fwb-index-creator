package sub.fwb.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.Model;

public class MainControllerTest {

	private MyMocks mock;
	private MainController mainController = new MainController();
	private Model mockModel = mock(Model.class);

	@Before
	public void setUp() throws Exception {
		mock = new MyMocks(mainController);
	}

	@Test
	public void shouldShortCircuitWhenLockExists() throws Exception {
		when(mock.lock.exists()).thenReturn(true);
		String nextPage = mainController.index(mockModel);
		assertEquals("started", nextPage);
	}

	@Test
	public void shouldExecuteGitAndGoToIndex() throws Exception {
		String nextPage = mainController.index(mockModel);

		verify(mock.git).init();
		verify(mock.git).pull();
		verify(mock.git).getLastCommitMessage();
		assertEquals("index", nextPage);
	}

	@Test
	public void shouldShortCircuitWhenStarted() throws Exception {
		when(mock.lock.exists()).thenReturn(true);
		mainController.importstaging(mockModel);

		verify(mock.runner, times(0)).run();
		verify(mock.lock, times(0)).create();
	}

	@Test
	public void shouldStartRunner() throws Exception {
		when(mock.lock.exists()).thenReturn(false);
		mainController.importstaging(mockModel);

		verify(mock.runner).run();
		verify(mock.lock).create();
	}

}
