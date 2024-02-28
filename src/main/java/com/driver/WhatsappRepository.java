package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 1;
        this.messageId = 1;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(!userMobile.isEmpty()&&userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
       if(users.size()==2){
           Group group=new Group(users.get(1).getName(),2);
           adminMap.put(group,users.get(0));
           groupUserMap.put(group,users);
           return group;
       }
       else{
           String s = "Group" + " " + customGroupCount;
           Group group=new Group(s,users.size());
           adminMap.put(group,users.get(0));
           groupUserMap.put(group,users);
          customGroupCount++;
           return group;
       }
}

    public int createMessage(String content) {
    Message message=new Message(messageId,content);
    messageId++;
    return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws  Exception {

        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        senderMap.put(message,sender);
       List<User> userList=new ArrayList<>(groupUserMap.get(group));
       for(User list:userList) {
           if (list.getMobile().equals(sender.getMobile())) {
               if (groupMessageMap.containsKey(group)) {
                   groupMessageMap.get(group).add(message);
                   return groupMessageMap.get(group).size();
               } else {
                   List<Message> messageList = new ArrayList<>();
                   messageList.add(message);
                   groupMessageMap.put(group, messageList);
                   return messageList.size();
               }
           }
       }
       throw new Exception("You are not allowed to send message");
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{

      if(!groupUserMap.containsKey(group)){
        throw new Exception("Group does not exist");
    }

      if(!approver.getMobile().equals(adminMap.get(group).getMobile())){
          throw new Exception("Approver does not have rights");
        }
      List<User> userList=new ArrayList<>(groupUserMap.get(group));
      for(User x:userList){
          if(x.getMobile().equals(user.getMobile())){
              adminMap.put(group,user);
              return "SUCCESS";
          }
      }
      throw new Exception("User is not a participant");
    }

    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        for(Group x:groupUserMap.keySet()) {
            List<User> list = new ArrayList<>(groupUserMap.get(x));
            for (User f : list) {
                if (f.getMobile().equals(user.getMobile())) {
                    if (adminMap.get(x).getMobile().equals(user.getMobile())) {
                        throw new Exception("Cannot remove admin");
                    } else {
                        List<Message> m = new ArrayList<>(groupMessageMap.get(x));
                        for (Message g : m) {
                            if (senderMap.get(g).getMobile().equals(user.getMobile())) {
                                m.remove(g);
                                senderMap.remove(g);
                            }
                        }
                        groupUserMap.get(x).remove(user);
                        x.setNumberOfParticipants(x.getNumberOfParticipants() - 1);
                        return groupUserMap.get(x).size() + groupMessageMap.get(x).size() + senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> list = new ArrayList<>();
        for(Message m : senderMap.keySet()){
            if(m.getTimestamp().compareTo(start)>0 && m.getTimestamp().compareTo(end)<0){
                list.add(m);
            }
        }
        Collections.sort(list,(a,b)->a.getTimestamp().compareTo(b.getTimestamp()));
        if(k>list.size()){
            throw new Exception("K is greater than the number of messages");
        }
        return list.get(k-1).getContent();
    }
    }

