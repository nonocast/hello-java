CC=gcc
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_161.jdk/Contents/Home

# $@: libcalc.jnilib
# $^: cn_nonocast_App.c
libcalc.jnilib : cn_nonocast_App.c
	$(CC) -shared -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -L~/lib -lcalc $^ -o $@