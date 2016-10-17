OpenSM Subnet Monitor/Management Tools (SMT)
=========================
by Tim Meier, [meier3@llnl.gov](mailto:meier3@llnl.gov)

**SMT** is a collection of Infiniband diagnostic utilities.

Released under the GNU LGPL, `LLNL-CODE-673346`.  See the `LICENSE`
file for details.

Overview
-------------------------

This java package is designed to provide a set of control and diagnostic tools for
Infiniband fabrics that are managed by OpenSM and use the OpenSM Monitoring Service ( **OMS**).

To the extent that it is possible, these tools will provide similar functionality
and results as the native `infiniband-diags` package.

These tools are designed to primarily utilize the **OMS**, and therefore should
be as scalable as OpenSM.  They operate "out of band" and do not communicate with the
individual nodes, so should be fast and should not disturb the fabric.

Since these tools use the OMS, they only know what the SM, SA, and PM know.
If any of these managers are *bad* (stale, corrupted, wedged), then the tools will also
be *bad*.

For this reason, the native tools that query the hardware/nodes directly will
always be necessary.

Man pages are available and in addition each command provides basic command line help.

* smt
* smt-about
* smt-console
* smt-config
* smt-event
* smt-fabric
* smt-file
* smt-gui
* smt-help
* smt-id
* smt-link
* smt-multicast
* smt-node
* smt-port
* smt-partition
* smt-priv
* smt-record
* smt-route
* smt-server
* smt-system
* smt-top
* smt-utilize