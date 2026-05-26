import java.util.ArrayList;

// read tsv file
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

// Exception handling
import java.io.IOException;

// handle TSV logic here e.g reading, adding, removing data
// getHeaderIdx(): For display command. Get index from header name. Return -1 if not found.

public class TSV {
    public ArrayList<String[]> data;
    public String[] header;
    public String filePath;

    TSV(String filePath) { // initialize TSV
        this.data = new ArrayList<>();
        this.filePath = filePath;
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(this.filePath));
            this.header = br.readLine().split("\t", -1);

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t", -1);
                this.data.add(values);                
            }
        } catch(IOException e) {
            System.out.print(e);
        }
    }

    public int idxToAppend(int id) { // utilize binary search since id is sorted.
        int i = 0;
        int j = data.size()-1;

        while (i <= j) {
            int middle = (i+j) / 2;
            int val = Integer.parseInt(data.get(middle)[0]);

            if (val == id) return -1; // -1 means id exists

            if (id > val) i = middle+1;
            else j = middle-1;
        }

        return i; // where row will be appended
    }

    private void refresh() {
        BufferedWriter bw;
        String headers = String.join("\t", this.header); // create one string with headers separated by tabs

        try {
            bw = new BufferedWriter(new FileWriter(this.filePath));
            bw.write(headers); // write headers

            for (int i = 0; i < data.size(); i++) {
                bw.newLine(); // new line every start
                String[] rowData = this.data.get(i);
                String row = String.join("\t", rowData); // write row
                bw.write(row);
            }

            bw.flush();
            bw.close();
        } catch(IOException e) {
            System.out.print(e);
        }
    }

    public void add(String[] rowAdded) {
        int id = Integer.parseInt(rowAdded[0]);
        data.add(idxToAppend(id), rowAdded);
        refresh();
    }

    public void remove(int[] indices) {
        for (int i = indices.length-1; i >= 0; i--) {
            data.remove(indices[i]);
        }
        refresh();
    }

    public int getHeaderIdx(String headerName) { // helper function to convert string header to index. If not found return -1.
        for (int i = 0; i < this.header.length; i++) {
            if (this.header[i].equals(headerName)) return i; 
        }
        return -1;
    }

    public void modify(Object[][] modifiedRows) { // format is [[rowIdx, columnIdx[], values[]]
        BufferedWriter bw;
        String headers = String.join("\t", this.header); // create one string with headers separated by tabs
        int i_modified = 0; // additional pointer to track current progress of modifiedRows

        try {
            bw = new BufferedWriter(new FileWriter(this.filePath));
            bw.write(headers); // write headers

            for (int i = 0; i < data.size(); i++) {
                bw.newLine(); // new line every start

                String[] rowData = this.data.get(i);
                
                // modify row data
                int mRowIdx = -1;
                if (i_modified < modifiedRows.length) mRowIdx = (int) modifiedRows[i_modified][0];
                if (i == mRowIdx) { // if index matches, modify the rowData
                    int[] mColumnIdx = (int[]) modifiedRows[i_modified][1]; // get column idx to be modified
                    String[] mValues = (String[]) modifiedRows[i_modified][2]; // get values

                    for (int j = 0; j < mColumnIdx.length; j++) {
                        int mIdx = mColumnIdx[j];
                        String mValue = mValues[j];

                        rowData[mIdx] = mValue; // Modify
                    }
                    i_modified++;
                }

                // write row
                String row = String.join("\t", rowData);
                bw.write(row);
            }

            bw.flush();
            bw.close();
        } catch(IOException e) {
            System.out.print(e);
        }
        
    }
}

