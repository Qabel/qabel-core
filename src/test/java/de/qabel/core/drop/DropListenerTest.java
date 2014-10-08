package de.qabel.core.drop;

import java.util.Date;
import org.junit.Test;

public class DropListenerTest {

	@Test
	public <T extends ModelObject> void dropListenerTest() {

		DropListener dl1 = new DropListener1();
		DropListener dl2 = new DropListener2();

		ModelObject1 m = new ModelObject1();
		m.content = "payload data";

		DropController dc = new DropController();
		dc.register(m, dl1);
		dc.register(m, dl2);

		DropMessage<ModelObject1> dm = new DropMessage<ModelObject1>();
		Date date = new Date(1412687357);

		dm.setTime(date);
		dm.setSender("foo");
		dm.setData(m);
		dm.setAcknowledgeID("bar");
		dm.setVersion(1);
		dm.setModelObject(ModelObject1.class);

		dc.handleDrop(dm);
	}
}
