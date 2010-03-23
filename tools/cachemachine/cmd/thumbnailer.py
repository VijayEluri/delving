"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)


 Build thumbnails

 both FULL_DOC & BRIEF_DOC versions from files found in ORIGINAL


 TODO:
   analyse errors, extrace uri hash and log in db that this is a bad item
   and move it to DIR_BAD_ORIGINAL (if it was an original)
"""

import os
import sys
import time
import subprocess
import exceptions


import settings


"""
=====================================

Module specific settings, should be set in the global local_settings.py

"""

"""
 Url structure for generated thumbnails, can be one of

 1 = - old style (pre 0.6)

        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.FULL_DOC.jpg
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.BRIEF_DOC.jpg

 2 = current style
        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as
        FULL_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg
        BRIEF_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg

"""
try:
    URL_STRUCTURE = settings.THUMBNAILS_STRUCTURE
except:
    URL_STRUCTURE = 2
if not URL_STRUCTURE in (1,2):
    raise exceptions.NotImplementedError('invalid THUMBNAILS_STRUCTURE')

"""
Normaly we would generate thumbs on dir level, when run on os x - at least my system
mogrify dies a terrible death if any of the "images" happens to be a wordfile or
similar, hanging, when process is killed the rest of that dir is not processed
In that case iteration on file level is better - slower but only the bad image is lost
since it couldnt be generated anyway no actual data is lost.
"""
try:
    ITERATE_ON_DIR = settings.THHUMBNAIL_PER_DIRECTORY
except:
    ITERATE_ON_DIR = True


INTERVALL_PROGRES = 60
IMG_CMD = 'mogrify'
REPOSITORY = 'repository'



class GenerateThumbnails(object):
    def __init__(self):
        self.error_count = 0
        self.t0 = time.time()
        self.file_count = 0



    def fix_names(self):
        """
        FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.BRIEF_DOC.jpg
        FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.FULL_DOC.jpg
        """
        self.move_one_tree(settings.DIR_FULL_DOC)
        self.move_one_tree(settings.DIR_BRIEF_DOC)


    def move_one_tree(self, file_group):
        base_dir = os.path.join(settings.MEDIA_ROOT, file_group)
        for dirpath, dirnames, filenames in os.walk(base_dir):
            if dirpath == base_dir:
                continue
            if self.t0 + INTERVALL_PROGRES  < time.time():
                # Show progress
                msg = '%s/%s - moved files: %i' % (file_group, sub_dir, self.file_count)
                print msg
                self.t0 = time.time()
            sub_dir = os.path.split(dirpath)[1]
            dest_dir = os.path.join(settings.MEDIA_ROOT, REPOSITORY, sub_dir)
            if not os.path.exists(dest_dir):
                os.mkdir(dest_dir)
            for filename in filenames:
                fname_src = '%s/%s' % (dirpath, filename)
                fname_dest = '%s/%s.%s.jpg' % (dest_dir,os.path.splitext(filename)[0],file_group)
                os.rename(fname_src, fname_dest)
                self.file_count += 1
                #print 'would have moved %s to %s' % (fname_src, fname_dest)
                #sys.exit(1)







    def run(self):
        base_dir = os.path.join(settings.MEDIA_ROOT, settings.DIR_ORIGINAL)
        for dirpath, dirnames, filenames in os.walk(base_dir):
            if dirpath == base_dir:
                continue
            sub_dir = os.path.split(dirpath)[1]
            #if sub_dir < '85D':
            #    continue
            if ITERATE_ON_DIR:
                # do a dir at a time
                if filenames:
                    self.create_imgs(dirpath, sub_dir)
            else: # do one file at a time
                for filename in filenames:
                    if os.path.splitext(filename)[1] == '.original':
                        self.create_one_image(dirpath, sub_dir, filename)
            pass
        if error_count:
            print 'Detected %i errors processing the tree' % error_count
        return

    def create_one_image(self, dir_path, sub_dir, filename):
        if 1:
            raise exceptions.NotImplementedError('check this code!')
        if self.t0 + INTERVALL_PROGRES  < time.time():
            # Show progress
            msg = sub_dir
            if self.error_count:
                msg += '\terrors found %i' % self.error_count
            print msg
            self.t0 = time.time()
        #
        #  FULL_DOC
        #
        dest_full = os.path.join(settings.MEDIA_ROOT, settings.DIR_FULL_DOC, sub_dir)
        cmd = ['%s -path %s' % (IMG_CMD, dest_full)]
        cmd.append('-format jpg')
        cmd.append('-define jpeg:size=260x200')
        cmd.append('-thumbnail 200x %s' % os.path.join(dir_path, filename))
        p = subprocess.Popen(' '.join(cmd), shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE, close_fds=True)
        retcode = p.wait()
        if retcode:
            self.error_count += 1
            print '*** Error generating a FULL_DOC'
            err_msg = p.stderr.read()
            print err_msg
            return # wont be able to do a BRIEF_DOC if FULL_DOC failed

        #
        # BRIEF_DOC
        #
        """
        mogrify -path BRIEF_DOC/subdir1/subdir2
            -format jpg
            -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg

        """
        dest_brief = os.path.join(settings.MEDIA_ROOT, settings.DIR_BRIEF_DOC, sub_dir)
        cmd = ['%s -path %s' % (IMG_CMD, dest_brief)]
        cmd.append('-format jpg')
        fname_brief = os.path.join(dest_full, os.path.splitext(filename)[0]) + '.jpg'
        cmd.append('-thumbnail x110 %s' % fname_brief)
        p = subprocess.Popen(' '.join(cmd), shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE, close_fds=True)
        retcode = p.wait()
        if retcode:
            self.error_count += 1
            print '*** Error generating a BRIEF_DOC'
            err_msg = p.stderr.read()
            print err_msg
            #parse_error(err_msg): Skip analyse for the moment...
        return



    def create_imgs(self, dir_path, sub_dir):
        #
        #  FULL_DOC
        #
        if 1:
            raise exceptions.NotImplementedError('check this code!')
        if URL_STRUCTURE == 1:
            dest_full = os.path.join(settings.MEDIA_ROOT, settings.DIR_FULL_DOC, sub_dir)
        else:
            dest_full = os.path.join(settings.MEDIA_ROOT, settings.DIR_FULL_DOC, sub_dir)

        cmd = ['%s -path %s' % (IMG_CMD, dest_full)]
        cmd.append('-format jpg')
        cmd.append('-define jpeg:size=260x200')
        cmd.append('-thumbnail 200x %s/*.original' % dir_path)
        p = subprocess.Popen(' '.join(cmd), shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE, close_fds=True)
        retcode = p.wait()
        if retcode:
            err_msg = p.stderr.read()
            print '*** Error'
            self.error_count += 1
            print err_msg
            if 0: #not parse_error(err_msg): Skip analyse for the moment...
                sys.exit(1)


        #
        # BRIEF_DOC
        #
        """
        mogrify -path BRIEF_DOC/subdir1/subdir2
            -format jpg
            -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg

        """
        dest_brief = os.path.join(settings.MEDIA_ROOT, settings.DIR_BRIEF_DOC, sub_dir)
        cmd = ['%s -path %s' % (IMG_CMD, dest_brief)]
        cmd.append('-format jpg')
        cmd.append('-thumbnail x110 %s/*.jpg' % dest_full)
        p = subprocess.Popen(' '.join(cmd), shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE, close_fds=True)
        retcode = p.wait()
        if retcode:
            err_msg = p.stderr.read()
            print '*** Error'
            self.error_count += 1
            print err_msg
            if 0: #not parse_error(err_msg): Skip analyse for the moment...
                sys.exit(1)
        return



    def parse_error(self, err_msg):
        parts = err_msg.split(IMG_CMD)
        if len(parts) == 1:
            print 'Error analyse failed, Couldnt understand this at all'
            return False
        for line in parts[1:]: # we skip empty first
            parts2 = line.split(settings.MEDIA_ROOT)
            if len(parts2) == 1:
                print 'Error analyse failed, didnt find the path part'
                return False
            rel_fname = parts2[1].split("'")[0]
            if os.path.isabs(rel_fname):
                rel_fname = rel_fname[1:] # remove initial dir indicator
            full_path = os.path.join(settings.MEDIA_ROOT, rel_fname)
            if not os.path.exists(full_path):
                print 'Error analyse failed, what we thougt was a file-ref wasnt', full_path
                return False
            #
            # Ok now we now we have found a full_path to the offending file
            # time to break it down in parts and reconstruct the uri-hash, so
            # we can inform the database that we couldnt handle this item
            pass
        pass

