#!/bin/bash
(cat qabel-drop/drop.pid | xargs kill && rm qabel-drop/drop.pid) || echo "failed to kill drop server"
(cat qabel-accounting/accounting.pid | xargs kill && rm qabel-accounting/accounting.pid) || echo "failed to kill accounting server"
(cat qabel-block/block.pid | xargs kill && rm qabel-block/block.pid) || echo "failed to kill block server"


