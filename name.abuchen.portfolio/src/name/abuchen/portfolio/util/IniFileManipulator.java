package name.abuchen.portfolio.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IniFileManipulator
{
    private static final String VM_ARGS = "-vmargs"; //$NON-NLS-1$
    private static final String NL = "-nl"; //$NON-NLS-1$
    private static final String CLEAR_PERSISTED_STATE = "-clearPersistedState"; //$NON-NLS-1$

    private List<String> lines = new ArrayList<String>();
    private boolean isDirty = false;

    public void load() throws IOException
    {
        lines = Files.readAllLines(getIniFile(), Charset.defaultCharset());
    }

    public void save() throws IOException
    {
        Files.write(getIniFile(), lines, Charset.defaultCharset());
        isDirty = false;
    }

    public Path getIniFile()
    {
        String eclipseLauncher = System.getProperty("eclipse.launcher"); //$NON-NLS-1$

        Path path = Paths.get(eclipseLauncher);

        String executable = path.getFileName().toString();
        int p = executable.lastIndexOf('.');
        String iniFileName = (p > 0 ? executable.substring(0, p) : executable) + ".ini"; //$NON-NLS-1$

        return path.getParent().resolve(iniFileName);
    }

    /* for testing */List<String> getLines()
    {
        return this.lines;
    }

    /* for testing */void setLines(List<String> lines)
    {
        this.lines = new ArrayList<String>(lines);
        this.isDirty = false;
    }

    public String getLanguage()
    {
        for (int ii = 0; ii < lines.size(); ii++)
        {
            String line = lines.get(ii);
            if (line.trim().equals(NL) && ii + 1 < lines.size())
                return lines.get(ii + 1);
        }

        return null;
    }

    public void setLanguage(String locale)
    {
        for (int ii = 0; ii < lines.size(); ii++)
        {
            String line = lines.get(ii);
            if (line.trim().equals(NL))
            {
                String oldValue = lines.set(ii + 1, locale);
                isDirty = !locale.equals(oldValue);
                return;
            }
            else if (line.trim().equals(VM_ARGS))
            {
                lines.add(ii, NL);
                lines.add(ii + 1, locale);
                isDirty = true;
                return;
            }
        }

        // -vmargs separator not found
        lines.add(NL);
        lines.add(locale);
        isDirty = true;
    }

    public void clearLanguage()
    {
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext())
        {
            String line = iterator.next();
            if (line.trim().equals(NL))
            {
                iterator.remove();
                if (iterator.hasNext())
                {
                    iterator.next();
                    iterator.remove();
                }
                isDirty = true;
                return;
            }
        }
    }

    public void setClearPersistedState()
    {
        for (int ii = 0; ii < lines.size(); ii++)
        {
            String line = lines.get(ii);
            if (line.trim().equals(CLEAR_PERSISTED_STATE))
            {
                return; // already set
            }
            else if (line.trim().equals(VM_ARGS))
            {
                lines.add(ii, CLEAR_PERSISTED_STATE);
                isDirty = true;
                return;
            }
        }

        // -vmargs not found
        lines.add(CLEAR_PERSISTED_STATE);
        isDirty = true;
    }

    public void unsetClearPersistedState()
    {
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext())
        {
            String line = iterator.next();
            if (line.trim().equals(CLEAR_PERSISTED_STATE))
            {
                iterator.remove();
                isDirty = true;
                return;
            }
        }
    }

    public boolean isDirty()
    {
        return isDirty;
    }
}
