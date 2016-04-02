%define java_package_name SubnetMonitorTool
%define osm_config_dir etc/opensm-plugin

Name:           	opensm-smt-java
Version:        	2.01
Release:        	51%{?dist}
Summary:        	Subnet monitoring tools for OpenSM, that use the OpenSM Monitoring Service

Group:          	Development/Libraries
License:        	GPL/BSD
BuildRoot: 			%{_tmppath}/%{name}-%{version}
Source0:        	%{name}-%{version}.tar.gz
BuildArch:      	noarch

BuildRequires:  	java-devel >= 1:1.6.0
Requires:       	java >= 1:1.6.0
Requires:			opensm-client-server-java >= 2.01-69, llnl-ldapotp-clt-java >= 2.01-35
BuildRequires:      opensm-client-server-java >= 2.01-69, llnl-ldapotp-clt-java >= 2.01-35
BuildRequires:  	jpackage-utils
BuildRequires:  	ant >= 1.6

Prefix:				/usr/share/java

%description
This package includes a collection of diagnostic and monitoring tools that
use the OpenSM Monitoring Service (OMS).  They are, in a sense, a front-end to
the OMS.  They are intended to be a somewhat higher level tool set than the
normal diags and utils.
See /usr/share/java/SubnetMonitorTool/bin/ for tools.

%prep
%setup -q

%build

%install
[ "%{buildroot}" != "/" ] && rm -rf %{buildroot}
mkdir -p $RPM_BUILD_ROOT%{_javadir}
cd $RPM_BUILD_ROOT%{_javadir}
tar -xzf %{SOURCE0}
mv $RPM_BUILD_ROOT%{_javadir}/%{name}-%{version} $RPM_BUILD_ROOT%{_javadir}/%{java_package_name}
cd $RPM_BUILD_ROOT%{_javadir}/%{java_package_name}
rm -f *.spec
ln -s %{java_package_name}-*.jar %{java_package_name}.jar

mkdir -p $RPM_BUILD_ROOT/%{osm_config_dir}
mv $RPM_BUILD_ROOT%{_javadir}/%{java_package_name}/%{osm_config_dir} $RPM_BUILD_ROOT/etc

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(644,root,root,755)
%{_javadir}/*
%config(noreplace) /%{osm_config_dir}/*.config


%defattr(755,root,root,755)
%{_javadir}/%{java_package_name}/bin/*


%changelog
* Thu Nov 19 2015 Tim Meier <meier3@llnl.gov> 2.01-51
- version 2.01 release 51
* Wed Nov 18 2015 Tim Meier <meier3@llnl.gov> 2.0.0-51
- support for EDR, system guids, and smt-utilize
* Tue Jul 21 2015 Tim Meier <meier3@llnl.gov> 2.0.0-49
- smt-route enhancements, plus feature to extract timestamps from history file
* Wed May 20 2015 Tim Meier <meier3@llnl.gov> 2.0.0-47
- new search capabilities, plus hotkey support
* Fri Apr 17 2015 Tim Meier <meier3@llnl.gov> 2.0.0-45
- heat maps, hop counts, and route balance features
* Mon Mar  2 2015 Tim Meier <meier3@llnl.gov> 2.0.0-43
- increased memory, to accomodate larger fabrics, enhanced version support
* Wed Feb 25 2015 Tim Meier <meier3@llnl.gov> 2.0.0-41
- improved several cmd line tools, handled path utilization
* Tue Feb 17 2015 Tim Meier <meier3@llnl.gov> 2.0.0-37
- handle HCA port guid differences, fixed port formatting
* Wed Feb  4 2015 Tim Meier <meier3@llnl.gov> 2.0.0-35
- converted to support v2.0 of OMS.  Additional tools.
* Thu Oct 23 2014 Tim Meier <meier3@llnl.gov> 1.0.0-31
- added smt-help plus minor package fixes
* Fri Oct 17 2014 Tim Meier <meier3@llnl.gov> 1.0.0-29
- snapshot release, minor package fixes
* Tue Oct 14 2014 Tim Meier <meier3@llnl.gov> 1.0.0-27
- supported detailed link info, plus initial path and route support
* Tue Apr  1 2014 Tim Meier <meier3@llnl.gov> 1.0.0-23
- included MAD counter diffs,graphs and Subnet Tree panel
* Thu Mar 13 2014 Tim Meier <meier3@llnl.gov> 1.0.0-21
- minimized logging, improved build system to include release, trap
  run-away connection attempts.
* Thu Mar  6 2014 Tim Meier <meier3@llnl.gov> 1.0.0-19
- numerous smt-gui additions, including initial help capability
* Tue Dec 17 2013 Tim Meier <meier3@llnl.gov> 1.0.0-17
- Xmas snapshot, includes new (early release) smt-gui
* Tue Oct 15 2013 Tim Meier <meier3@llnl.gov> 1.0.0-15
- Furlough snapshot
* Fri Oct  4 2013 Tim Meier <meier3@llnl.gov> 1.0.0-13
- Fixed convenience scripts to start with magic kernel shell directive
* Thu Sep 26 2013 Tim Meier <meier3@llnl.gov> 1.0.0-11
- fixed xfer & rcv rate calculation
* Fri Sep 20 2013 Tim Meier <meier3@llnl.gov> 1.0.0-9
- added smt-link command, and filter capability
* Tue Sep 10 2013 Tim Meier <meier3@llnl.gov> 1.0.0-7
- fixed smt-console startup bug, added a player for history files
* Fri Aug  9 2013 Tim Meier <meier3@llnl.gov> 1.0.0-5
- improved some commands and added several new commands and scripts
* Fri May  3 2013 Tim Meier <meier3@llnl.gov> 1.0.0-2
- added several commands, including smt-console
* Wed Jan 18 2012 Tim Meier <meier3@llnl.gov> 1.0.0-1
- Initial Version.
