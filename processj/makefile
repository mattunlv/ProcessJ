COMPILER:=g++
CPPFLAGS:=-Wall -Wextra -g -pedantic -pthread -std=c++17 -masm=intel#-fsanitize=address
OBJFLAGS:=-c -fPIC
TARGET:=cppruntime# Originally pj_runtime
NAMESPACE:=ProcessJ
ROOT:=src/$(TARGET)/
RUNTIMEFLAGS:=-L$(ROOT)lib -l$(TARGET)

# ----------
# Extensions

HPPCONST:=.hpp
CPPCONST:=.cpp
OBJCONST:=.o
GCHCONST:=.gch
AAACONST:=.a

ALLHPPCONST:=*$(HPPCONST)
ALLCPPCONST:=*$(CPPCONST)
ALLOBJCONST:=*$(OBJCONST)
ALLGCHCONST:=*$(PCHCONST)

# -----------
# Directories

BIN_DIR:=$(ROOT)bin
INCLUDE_DIR:=$(ROOT)include
SOURCE_DIR:=$(ROOT)src
LIB_DIR:=$(ROOT)lib
OBJ_DIR:=$(ROOT)obj
RUNTIME_DIR:=runtime
TESTS_DIR:=tests
UTILITIES:=utilities

# ----------
# Root Paths

INCLUDEPATH:=-I$(INCLUDE_DIR)/
SOURCEPATH:=$(SOURCE_DIR)/
LIBRARYPATH:=$(LIB_DIR)/
OBJECTPATH:=$(OBJ_DIR)/

# -------------
# Include Paths

RUNTIME_INCLUDE_PATH:=$(INCLUDEPATH)$(RUNTIME_DIR)/
TESTS_INCLUDE_PATH:=$(INCLUDEPATH)$(TESTS_DIR)/
UTILITIES_INCLUDE_PATH:=$(INCLUDEPATH)$(UTILITIES)/

# -----------
# Source Path

RUNTIME_SOURCE_PATH:=$(SOURCEPATH)$(RUNTIME_DIR)/
TESTS_SOURCE_PATH:=$(SOURCEPATH)$(TESTS_DIR)/
UTILITIES_SOURCE_PATH:=$(SOURCEPATH)$(UTILITIES)/

# --------
# Includes

INCLUDES:=-Ilib/C++ $(INCLUDEPATH) $(RUNTIME_INCLUDE_PATH) $(TESTS_INCLUDE_PATH) $(UTILITIES_INCLUDE_PATH)

# -------
# Targets

$(LIB_DIR)/$(TARGET)$(AAACONST):
	reset && clear
	@echo "Checking for lib directory."
	if [ ! -d "./$(RUNTIME_DIR)lib" ]; then mkdir -p $(RUNTIME_DIR)lib; fi
	if [ ! -d "./$(RUNTIME_DIR)obj" ]; then mkdir -p $(RUNTIME_DIR)obj; fi
	@echo "Creating objects."
	$(COMPILER) $(CPPFLAGS) $(UTILITIES_INCLUDE_PATH) $(RUNTIME_INCLUDE_PATH) $(OBJFLAGS) -o $(OBJECTPATH)$(TARGET)$(OBJCONST) $(RUNTIME_SOURCE_PATH)$(ALLCPPCONST)
	$(COMPILER) $(CPPFLAGS) $(UTILITIES_INCLUDE_PATH) $(OBJFLAGS) -o $(OBJECTPATH)$(TARGET)_utilities$(OBJCONST) $(UTILITIES_SOURCE_PATH)$(ALLCPPCONST)
	@echo "Creating library."
	#ar cvq $(OBJECTPATH)$(TARGET)$(OBJCONST) $(OBJECTPATH)$(TARGET)_utilities$(OBJCONST)
	#ranlib $(OBJECTPATH)$(TARGET)$(OBJCONST)
	#rm -f  $(OBJECTPATH)$(TARGET)_utilities$(OBJCONST)

$(BIN_DIR)/$(TESTS_DIR):
	reset && clear
	@echo "Creating Tests."
	$(COMPILER) $(CPPFLAGS) $(TESTS_INCLUDE_PATH) -o $(TESTS_SOURCE_PATH) $(RUNTIMEFLAGS)

objects:
	$(COMPILER) $(CPPFLAGS) $(UTILITIES_INCLUDE_PATH) $(RUNTIME_INCLUDE_PATH) $(OBJFLAGS) -o $(OBJECTPATH)$(TARGET)$(OBJCONST) $(RUNTIME_SOURCE_PATH)$(ALLCPPCONST)
	$(COMPILER) $(CPPFLAGS) $(UTILITIES_INCLUDE_PATH) $(OBJFLAGS) -o $(OBJECTPATH)$(TARGET)_utilities$(OBJCONST) $(UTILITIES_SOURCE_PATH)$(ALLCPPCONST)

build:
	$(LIB_DIR)/$(TARGET)$(AAACONST)
	$(COMPILER) $(CPPFLAGS) -o $@ $< $(RUNTIMEFLAGS)

clean:
	clear
	@echo "Cleaning"
ifeq (,$(wildcard $(BIN_DIR)/$(TARGET).dSYM))
	rm -rf $(BIN_DIR)/$(TARGET).dSYM
endif
ifeq (,$(wildcard $(BIN_DIR)/$(TARGET).o))
	rm -rf $(LIB_DIR)/*
	rm -rf $(OBJECTPATH)$(TARGET)$(OBJCONST)
	rm -rf $(OBJECTPATH)$(TARGET)_utilities$(OBJCONST)
	rm -rf $(BIN_DIR)/$(TARGET)
	rm -rf $(BIN_DIR)/$(TARGET).o
endif

