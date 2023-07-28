# SPDX-License-Identifier:	BSD-3-Clause
#
# Copyright 2019-2020 NXP
#
MAKEFLAGS += --warn-undefined-variables
EXTRA_CFLAGS ?=

# ifeq ($(CROSS_COMPILE),)
# $(error CROSS_COMPILE is not set!)
# endif

# IPC_UIO_MODULE_DIR needed for automatically inserting kernel module
IPC_UIO_MODULE_DIR ?=
ifeq ($(IPC_UIO_MODULE_DIR),)
IPC_UIO_MODULE_DIR := .
$(info IPC_UIO_MODULE_DIR is not set! using default value)
endif
$(info IPC_UIO_MODULE_DIR = $(IPC_UIO_MODULE_DIR))

# CC := $(CROSS_COMPILE)gcc
# AR := $(CROSS_COMPILE)ar
RM := rm -f

includes := -I./common -I./hw -I./os -I./common/os
CFLAGS += -Wall -g $(includes) #-DDEBUG
CFLAGS += $(EXTRA_CFLAGS)

lib_name = libipc-shm.a

ipc_uio_name = ipc-shm-uio
CFLAGS += -DIPC_UIO_MODULE_PATH=\"$(IPC_UIO_MODULE_DIR)/$(ipc_uio_name).ko\"
CFLAGS += -DIPC_UIO_MODULE_NAME=\"$(shell echo $(ipc_uio_name) | tr '-' '_')\"

# object file list
objs = common/ipc-shm.o common/ipc-queue.o os/ipc-os.o

%.o: %.c
	@echo 'Building lib file: $<'
	$(CC) -c $(CFLAGS) -o $@ $<
	@echo ' '

$(lib_name): $(objs)
	@echo 'Building target: $@'
	$(AR) rcs $(lib_name) $(objs)
	@echo ' '

clean:
	$(RM) $(objs) $(lib_name)

.PHONY: clean