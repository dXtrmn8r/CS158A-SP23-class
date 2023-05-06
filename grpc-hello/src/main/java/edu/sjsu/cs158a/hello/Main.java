package edu.sjsu.cs158a.hello;

import edu.sjsu.cs158a.hello.Messages.CodeRequest;
import edu.sjsu.cs158a.hello.Messages.CodeResponse;
import edu.sjsu.cs158a.hello.Messages.ListRequest;
import edu.sjsu.cs158a.hello.Messages.ListResponse;
import edu.sjsu.cs158a.hello.Messages.RegisterRequest;
import edu.sjsu.cs158a.hello.Messages.RegisterResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@Command
public class Main {
    @Command
    int add(@Parameters(paramLabel = "hostPort") String hostPort,
            @Parameters(paramLabel = "a") int a,
            @Parameters(paramLabel = "b") int b) {
        try {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();
            var stub = AddExampleGrpc.newBlockingStub(channel);
            var request = Messages.AddExampleRequest.newBuilder().setA(a).setB(b).build();
            var rsp = stub.add(request);
            System.out.println(rsp.getResult());
        } catch (StatusRuntimeException e) {
            System.out.println("problem communicating with " + hostPort);
        }
        return 0;
    }

    @Command
    int register(@Parameters(paramLabel = "hostPort") String hostPort,
                 @Parameters(paramLabel = "course") String courseName,
                 @Parameters(paramLabel = "ssid") int ssid,
                 @Parameters(paramLabel = "studentName") String studentName) {
        try {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();
            var stub = HelloGrpc.newBlockingStub(channel);
            var addCodeRequest = Messages.CodeRequest.newBuilder().setCourse(courseName).setSsid(ssid).build();
            var cRsp = stub.requestCode(addCodeRequest);

            if (cRsp.getRc() != 0) {
                int srvRc = cRsp.getRc();
                System.out.println("problem getting add code: "+srvRc);
                return srvRc;
            }

            int addCode = cRsp.getAddcode();

            var registerRequest = Messages.RegisterRequest.newBuilder()
                    .setAddCode(addCode).setSsid(ssid).setName(studentName).build();

            var rRsp = stub.register(registerRequest);

            if (rRsp.getRc() != 0) {
                int srvRc = rRsp.getRc();
                System.out.println("problem registering: "+srvRc);
                return srvRc;
            } else {
                System.out.println("registration successful");
            }
        } catch (StatusRuntimeException e) {
            System.out.println("problem communicating with " + hostPort);
        }
        return 0;
    }

    @Command
    int listStudents(@Parameters(paramLabel = "hostPort") String hostPort,
                 @Parameters(paramLabel = "course") String courseName) {
        try {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();
            var stub = HelloGrpc.newBlockingStub(channel);
            var listRequest = Messages.ListRequest.newBuilder().setCourse(courseName).build();
            var rsp = stub.list(listRequest);

            if (rsp.getRc() != 0) {
                int srvRc = rsp.getRc();
                System.out.println("problem listing students: "+srvRc);
                return srvRc;
            }

            TreeMap<Integer,String> regList = new TreeMap<>();
            TreeMap<Integer,Integer> regListAddCode = new TreeMap<>();

            List<Messages.RegisterRequest> registerationsList = rsp.getRegisterationsList();
            for (Messages.RegisterRequest regRequest : registerationsList) {
                int addCode = regRequest.getAddCode();
                int ssid = regRequest.getSsid();
                String studentName = regRequest.getName();
                regList.put(ssid, studentName);
                regListAddCode.put(ssid, addCode);
            }

            for (int ssid : regList.keySet()) {
                int addCode = regListAddCode.get(ssid);
                String studentName = regList.get(ssid);
                System.out.format("%d %d %s\n", addCode, ssid, studentName);
            }

        } catch (StatusRuntimeException e) {
            System.out.println("problem communicating with " + hostPort);
        }
        return 0;
    }

    @Command
    int server(@Parameters(paramLabel = "port") int port) throws InterruptedException {

        class HelloImpl extends HelloGrpc.HelloImplBase {
            private HashMap<Integer,Integer> addCodeStudentRecord = new HashMap<>();    // addCode,ssid
            private HashMap<Integer,Boolean> addCodeCourseRecord = new HashMap<>();     // addCode, course == CS158A?
            private HashMap<Integer,String> studentRecord = new HashMap<>();            // ssid, name
            private HashMap<Integer,Integer> cs158a = new HashMap<>();                  // ssid, addCode
            private int addCodeToGive = 1;
            private HashMap<Integer,Integer> cs158b = new HashMap<>();                  // ssid, addCode

            @Override
            public void requestCode(CodeRequest request, StreamObserver<CodeResponse> responseObserver) {

                var course = request.getCourse();
                int ssid = request.getSsid();

                int rc = 0;                 // assume that an add code will be issued
                if (!course.equals("CS158A") && !course.equals("CS158B"))
                    rc = 1;

                if (ssid < 100_000 || ssid > 90_000_000)
                    rc = 2;

                int addCode;
                synchronized (this) {
                    if (rc == 0) {
                        addCode = addCodeToGive;
                        addCodeStudentRecord.put(addCode, ssid);
                        addCodeCourseRecord.put(addCode, course.equals("CS158A"));
                        addCodeToGive++;
                    } else {
                        addCode = 0;
                    }
                }
                try {
                    Thread.sleep(3000,0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                var response = Messages.CodeResponse.newBuilder()
                        .setRc(rc).setAddcode(addCode).build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

            @Override
            public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
                int addCode = request.getAddCode();
                int ssid = request.getSsid();
                String studentName = request.getName();

                int rc = 0;                                             // assume that the student will be registered

                if (addCode >= addCodeToGive)                           // addCode has not been issued yet
                    rc = 1;
                else if (addCodeStudentRecord.get(addCode) != ssid)
                    rc = 2;
                else {                                                  // student will be registered
                    synchronized (this) {
                        studentRecord.put(ssid, studentName);

                        // add the student to the course
                        if (addCodeCourseRecord.get(addCode))           // is the add code for 158A?
                            cs158a.put(ssid, addCode);
                        else cs158b.put(ssid, addCode);
                    }
                }

                var response = Messages.RegisterResponse.newBuilder()
                        .setRc(rc).build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

            @Override
            public void list(ListRequest request, StreamObserver<ListResponse> responseObserver) {

                var courseCode = request.getCourse();
                int rc = 1;                                             // invalid course
                HashMap<Integer,Integer> classRoster = new HashMap<>();

                if (courseCode.equals("CS158A")) {
                    rc = 0;
                    classRoster = cs158a;
                } else if (courseCode.equals("CS158B")) {
                    rc = 0;
                    classRoster = cs158b;
                }

                var responseBuilder = Messages.ListResponse.newBuilder().setRc(rc);

                for (int ssid : classRoster.keySet()) {
                    int addCode = classRoster.get(ssid);
                    String studentName = studentRecord.get(ssid);

                    var regRequest = Messages.RegisterRequest.newBuilder()
                                    .setAddCode(addCode)
                                    .setSsid(ssid).setName(studentName).build();

                    responseBuilder.addRegisterations(regRequest);
                }

                var response = responseBuilder.build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }

        try {
            var server = ServerBuilder.forPort(port).addService(new HelloImpl()).build();
            server.start();
            server.awaitTermination();
        } catch (IOException e) {
            System.out.println("couldn't serve on " + port);
        }
        return 0;
    }
    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}